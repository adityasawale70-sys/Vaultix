package com.vaultix.service.impl;

import com.vaultix.dto.*;
import com.vaultix.entity.RefreshToken;
import com.vaultix.entity.User;
import com.vaultix.exception.UnauthorizedException;
import com.vaultix.repository.RefreshTokenRepository;
import com.vaultix.security.JwtService;
import com.vaultix.service.AuthService;
import com.vaultix.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService              userService;
    private final JwtService               jwtService;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final AuthenticationManager    authenticationManager;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    public AuthServiceImpl(
            UserService userService,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager) {

        this.userService           = userService;
        this.jwtService            = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager  = authenticationManager;
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Persist client-provided salt (if any) for key derivation metadata
        if (request.getSalt() != null) user.setSalt(request.getSalt());

        User savedUser = userService.registerUser(user, request.getPassword());
        log.info("New user registered: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return RegisterResponse.fromEntity(savedUser);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Spring Security AuthenticationManager validates credentials.
        // Throws BadCredentialsException on failure (handled by GlobalExceptionHandler).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Credentials are valid — record the successful login
        userService.recordSuccessfulLogin(request.getEmail());

        // Issue tokens
        String accessToken  = jwtService.generateAccessToken(request.getEmail());
        String rawRefresh   = issueRefreshToken(request.getEmail());

        log.info("User logged in: email={}", request.getEmail());
        // Return per-user salt (if available) so the client can derive keys without storing email
        User user = userService.findUserByEmail(request.getEmail());
        String salt = user != null ? user.getSalt() : null;
        return new LoginResponse(accessToken, rawRefresh, salt);
    }

    // ─── Refresh Token ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String rawToken  = request.getRefreshToken();
        String tokenHash = sha256(rawToken);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!rt.isActive()) {
            // Token is expired or revoked — force full re-login
            throw new UnauthorizedException("Refresh token has expired or been revoked. Please log in again.");
        }

        // Issue a new access token
        String newAccessToken = jwtService.generateAccessToken(rt.getUsername());

        log.debug("Access token refreshed for: email={}", rt.getUsername());
        return new RefreshTokenResponse(newAccessToken);
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String tokenHash = sha256(request.getRefreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revoked for: email={}", rt.getUsername());
        });
        // If token not found — silent success (idempotent logout)
    }

    // ─── Get Current User ────────────────────────────────────────────────────

    @Override
    public MeResponse getCurrentUser(String email) {
        User user = userService.findUserByEmail(email);
        return MeResponse.fromEntity(user);
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    /**
     * Creates and persists a new refresh token.
     * Returns the RAW (plain) token to send to the client.
     * Only the SHA-256 hash is stored in the DB.
     */
    private String issueRefreshToken(String email) {
        // Generate cryptographically random token
        String rawToken  = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);

        // Revoke any existing refresh tokens for this user (single active token per user)
        refreshTokenRepository.deleteAllByUsername(email);

        RefreshToken rt = new RefreshToken();
        rt.setUsername(email);
        rt.setTokenHash(tokenHash);
        rt.setExpiry(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS));
        rt.setRevoked(false);

        refreshTokenRepository.save(rt);
        return rawToken; // raw token → client; hash only → DB
    }

    /**
     * Computes a SHA-256 hex digest of the given input.
     * Used to hash refresh tokens before persisting to DB.
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JVM spec — this should never happen
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
