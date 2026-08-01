package com.vaultix.dto;

import com.vaultix.entity.PermissionLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VaultItemShareRequest {

    @NotBlank(message = "Target user email is required")
    @Email(message = "Must be a valid email address")
    private String sharedWithEmail;

    @NotNull(message = "Permission level is required")
    private PermissionLevel permissionLevel = PermissionLevel.READ;

    private Integer viewCountLimit;

    private LocalDateTime expiresAt;

    public String getSharedWithEmail() {
        return sharedWithEmail;
    }

    public void setSharedWithEmail(String sharedWithEmail) {
        this.sharedWithEmail = sharedWithEmail;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public Integer getViewCountLimit() {
        return viewCountLimit;
    }

    public void setViewCountLimit(Integer viewCountLimit) {
        this.viewCountLimit = viewCountLimit;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
