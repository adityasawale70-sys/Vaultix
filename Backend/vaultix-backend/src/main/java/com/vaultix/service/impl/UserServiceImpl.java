package com.vaultix.service.impl;

import com.vaultix.entity.AccountStatus;
import com.vaultix.entity.User;
import com.vaultix.exception.ResourceNotFoundException;
import com.vaultix.repository.UserRepository;
import com.vaultix.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User registerUser(User user, String rawPassword) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        // Set to ACTIVE directly for V1 (no email verification flow yet).
        // The schema supports PENDING → ACTIVE via email token — to be wired up in V1.1.
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }

    // ─── Find ─────────────────────────────────────────────────────────────────

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // ─── Login tracking ──────────────────────────────────────────────────────

    /**
     * Called after a successful authentication to:
     *  - record lastLoginAt timestamp
     *  - reset failedLoginAttempts counter
     *  - unlock the account if it was previously locked
     */
    @Override
    @Transactional
    public void recordSuccessfulLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);

            // Clear any temporary lockout
            if (user.getAccountLockedUntil() != null
                    && LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
                user.setAccountLockedUntil(null);
                user.setAccountStatus(AccountStatus.ACTIVE);
            }

            userRepository.save(user);
            log.debug("Recorded successful login for userId={}", user.getUserId());
        });
    }
}
