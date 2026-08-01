package com.vaultix.service;

import com.vaultix.dto.TotpSetupRequest;
import com.vaultix.entity.User;

public interface TotpService {

    String generateTotpSecret();

    String generateTotpUri(String username, String secret);

    boolean verifyTotp(String secret, String code);

    String getBase32EncodedSecret(String base32Secret);

    String getBase32SecretFromBase64(String base64Secret);
}