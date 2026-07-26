package com.vaultix.dto;

import com.vaultix.entity.User;

public class RegisterResponse {

    private Long userId;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public static RegisterResponse fromEntity(User user) {
        return new RegisterResponse(
                user.getUserId(),
                "Registration successful"
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
}