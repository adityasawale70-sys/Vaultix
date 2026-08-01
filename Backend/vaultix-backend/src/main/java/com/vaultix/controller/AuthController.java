package com.vaultix.controller;

import com.vaultix.dto.*;
import com.vaultix.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for all authentication operations.
 *
 * Public endpoints  (no JWT required):
 *   POST /api/auth/register
 *   POST /api/auth/login
 *   POST /api/auth/refresh
 *
 * Protected endpoints (JWT required):
 *   POST /api/auth/logout
 *   GET  /api/auth/me
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── POST /api/auth/register ──────────────────────────────────────────────

    /**
     * Register a new user account.
     * Returns 201 Created with userId on success.
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── POST /api/auth/login ─────────────────────────────────────────────────

    /**
     * Authenticate with email + password.
     * Returns both access token (short-lived, 15 min) and refresh token (7 days).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─── POST /api/auth/refresh ───────────────────────────────────────────────

    /**
     * Exchange a valid refresh token for a new access token.
     * Does NOT require an Authorization header — the refresh token IS the credential here.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    // ─── POST /api/auth/logout ────────────────────────────────────────────────

    /**
     * Revoke the supplied refresh token.
     * Requires a valid access token in the Authorization header.
     * Access tokens are stateless and cannot be revoked — they expire naturally.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ─── GET /api/auth/me ─────────────────────────────────────────────────────

    /**
     * Returns the authenticated user's profile.
     * Requires a valid access token in the Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        MeResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}