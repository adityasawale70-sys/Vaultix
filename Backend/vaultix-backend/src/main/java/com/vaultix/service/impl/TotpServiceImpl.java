package com.vaultix.service.impl;

import com.vaultix.dto.TotpSetupRequest;
import com.vaultix.entity.User;
import com.vaultix.repository.UserRepository;
import com.vaultix.service.TotpService;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;

@Service
public class TotpServiceImpl implements TotpService {

    private final UserRepository userRepository;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int TOTP_DIGITS = 6;

    public TotpServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String generateTotpSecret() {
        // Generate 20 random bytes and encode as base32 (160 bits -> good for SHA1)
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        Base32 b32 = new Base32();
        return b32.encodeToString(bytes).replace("=","");
    }

    @Override
    public String generateTotpUri(String username, String secret) {
        // Build otpauth URI compatible with authenticator apps
        // otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}&algorithm=SHA1&digits=6&period=30
        String issuer = "Vaultix";
        String account = username;
        String uri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                urlEncode(issuer), urlEncode(account), urlEncode(secret), urlEncode(issuer));
        return uri;
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }

    @Override
    public boolean verifyTotp(String secret, String code) {
        try {
            byte[] keyBytes = new Base32().decode(secret);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA1");
            long currentCounter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
            for (int i = -1; i <= 1; i++) {
                long counter = currentCounter + i;
                int generated = generateHotp(keySpec, counter);
                if (String.format("%06d", generated).equals(code)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int generateHotp(SecretKeySpec keySpec, long counter) throws Exception {
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(keySpec);
        byte[] hmac = mac.doFinal(counterBytes);
        int offset = hmac[hmac.length - 1] & 0x0F;
        int binary = ((hmac[offset] & 0x7f) << 24) | ((hmac[offset + 1] & 0xff) << 16)
                | ((hmac[offset + 2] & 0xff) << 8) | (hmac[offset + 3] & 0xff);
        int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
        return otp;
    }

    @Override
    public String getBase32EncodedSecret(String base32Secret) {
        return base32Secret;
    }

    @Override
    public String getBase32SecretFromBase64(String base64Secret) {
        // Convert base64 to base32
        byte[] decoded = java.util.Base64.getDecoder().decode(base64Secret);
        return new Base32().encodeToString(decoded).replace("=","");
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