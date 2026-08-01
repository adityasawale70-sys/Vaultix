package com.vaultix.controller;

import com.vaultix.dto.*;
import com.vaultix.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for all authentication operations.
 *
 * Public endpoints (no JWT required):
 * POST /api/auth/register
 * POST /api/auth/login
 * POST /api/auth/refresh
 *
 * Protected endpoints (JWT required):
 * POST /api/auth/logout
 * GET /api/auth/me
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        LoginResponse response = authService.login(request);

        if (response.getRefreshToken() != null) {

            boolean secure =
                    servletRequest.isSecure()
                            || "https".equalsIgnoreCase(servletRequest.getScheme());

            long maxAgeSeconds =
                    refreshTokenExpirationDays * 24 * 60 * 60;

            ResponseCookie cookie = ResponseCookie.from(
                            "vaultix_refresh_token",
                            response.getRefreshToken())
                    .httpOnly(true)
                    .secure(secure)
                    .path("/api")
                    .sameSite("Lax")
                    .maxAge(maxAgeSeconds)
                    .build();

            servletResponse.addHeader(
                    HttpHeaders.SET_COOKIE,
                    cookie.toString());

            // Remove refresh token from response body
            response.setRefreshToken(null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @CookieValue(value = "vaultix_refresh_token", required = false)
            String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshTokenRequest request =
                new RefreshTokenRequest(refreshToken);

        RefreshTokenResponse response =
                authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Logout user.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(value = "vaultix_refresh_token", required = false)
            String refreshToken,
            HttpServletResponse servletResponse) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(new LogoutRequest(refreshToken));
        }

        ResponseCookie cookie = ResponseCookie.from(
                        "vaultix_refresh_token",
                        "")
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        servletResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully"));
    }

    /**
     * Current authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        MeResponse response =
                authService.getCurrentUser(userDetails.getUsername());

        return ResponseEntity.ok(response);
    }
}