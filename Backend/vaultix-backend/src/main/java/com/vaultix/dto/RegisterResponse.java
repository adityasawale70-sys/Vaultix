package com.vaultix.dto;

import com.vaultix.entity.User;

public class RegisterResponse {

    private Long userId;
    private String message;
    private String salt; // optional per-user salt (base64)

    public RegisterResponse() {
    }

    public RegisterResponse(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public RegisterResponse(Long userId, String message, String salt) {
        this.userId = userId;
        this.message = message;
        this.salt = salt;
    }

    public static RegisterResponse fromEntity(User user) {
        return new RegisterResponse(
                user.getUserId(),
                "Registration successful",
                user.getSalt()
        );
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}