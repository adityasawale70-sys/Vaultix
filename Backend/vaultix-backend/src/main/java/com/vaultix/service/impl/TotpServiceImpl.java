package com.vaultix.service.impl;

import com.vaultix.dto.TotpSetupRequest;
import com.vaultix.entity.User;
import com.vaultix.repository.UserRepository;
import com.vaultix.service.TotpService;
import dev.samstevens.totp.Totp;
import dev.samstevens.totp.exceptions.InvalidKeyException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataImpl;
import dev.samstevens.totp.util.Base32;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TotpServiceImpl implements TotpService {

    private final UserRepository userRepository;
    private final Totp totp;

    public TotpServiceImpl(UserRepository userRepository, Totp totp) {
        this.userRepository = userRepository;
        this.totp = totp;
    }

    @Override
    public String generateTotpSecret() {
        return Base32.random();
    }

    @Override
    public String generateTotpUri(String username, String secret) {
        QrData data = new QrDataImpl.Builder()
                .label(username)
                .secret(secret)
                .issuer("Vaultix")
                .algorithm(QrDataImpl.Algorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        return totp.generateUri(data);
    }

    @Override
    public boolean verifyTotp(String secret, String code) {
        try {
            return totp.validateCode(secret, code);
        } catch (InvalidKeyException e) {
            return false;
        }
    }

    @Override
    public String getBase32EncodedSecret(String base32Secret) {
        return base32Secret;
    }

    @Override
    public String getBase32SecretFromBase64(String base64Secret) {
        return base64Secret;
    }

    @Transactional
    public void setupTotp(Long userId, TotpSetupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!verifyTotp(request.getTotpSecret(), request.getTotpCode())) {
            throw new RuntimeException("Invalid TOTP code");
        }

        user.setTotpSecret(request.getTotpSecret());
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableTotp(Long userId, String totpCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!verifyTotp(user.getTotpSecret(), totpCode)) {
            throw new RuntimeException("Invalid TOTP code");
        }

        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
    }

    @Transactional
    public void prepareTotpForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = generateTotpSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);
    }
}