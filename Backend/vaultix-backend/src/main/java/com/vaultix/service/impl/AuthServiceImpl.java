package com.vaultix.service.impl;

import com.vaultix.dto.LoginRequest;
import com.vaultix.dto.LoginResponse;
import com.vaultix.dto.RegisterRequest;
import com.vaultix.dto.RegisterResponse;
import com.vaultix.entity.RefreshToken;
import com.vaultix.entity.User;
import com.vaultix.repository.RefreshTokenRepository;
import com.vaultix.security.JwtService;
import com.vaultix.service.AuthService;
import com.vaultix.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserService userService,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        User savedUser = userService.registerUser(user, request.getPassword());
        return RegisterResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Generate both access and refresh tokens
        String accessToken = jwtService.generateAccessToken(request.getEmail());
        String refreshToken = createRefreshToken(request.getEmail());

        // Return both tokens in response
        return new LoginResponse(accessToken, refreshToken);
    }

    private String createRefreshToken(String email) {
        User user = userService.findUserByEmail(email);

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(email);
        refreshToken.setTokenHash(uuid().toString()); // Track UUID as token hash
        refreshToken.setExpiry(Instant.now().plus(Duration.ofMinutes(1440))); // 24-hour expiry
        refreshToken.setRevoked(false);

        // Save to database
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getTokenHash();
    }

    public void revokeRefreshToken(String tokenHash) {
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    // Add helper method to find user by email
    public User findUserByEmail(String email) {
        return userService.findUserByEmail(email);
    }

    // Generate UUID (simplified for demo; replace with proper UUID generator)
    private String uuid() {
        return "rt-" + java.util.UUID.randomUUID().toString().replaceAll("[-]", "") ;
    }
}

