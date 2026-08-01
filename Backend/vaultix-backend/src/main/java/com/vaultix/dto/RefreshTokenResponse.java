package com.vaultix.dto;

/**
 * Response payload for a successful token refresh.
 * Returns a brand-new access token (the refresh token itself stays the same
 * unless you implement full rotation — future enhancement).
 */
public class RefreshTokenResponse {

    private String accessToken;
    private String tokenType = "Bearer";

    public RefreshTokenResponse() {}

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
