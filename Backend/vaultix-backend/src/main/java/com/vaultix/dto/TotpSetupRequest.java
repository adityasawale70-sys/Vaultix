package com.vaultix.dto;

import jakarta.validation.constraints.NotBlank;

public class TotpSetupRequest {

    @NotBlank(message = "TOTP secret is required")
    private String totpSecret;

    @NotBlank(message = "TOTP code is required")
    private String totpCode;

    public TotpSetupRequest() {
    }

    public TotpSetupRequest(String totpSecret, String totpCode) {
        this.totpSecret = totpSecret;
        this.totpCode = totpCode;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }
}