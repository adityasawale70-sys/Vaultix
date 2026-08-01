package com.vaultix.dto;

import com.vaultix.entity.PermissionLevel;
import com.vaultix.entity.VaultItemShare;

import java.time.LocalDateTime;

public class VaultItemShareResponse {

    private Long shareId;
    private Long vaultItemId;
    private String itemTitle;
    private String sharedByEmail;
    private String sharedWithEmail;
    private PermissionLevel permissionLevel;
    private Integer viewCountLimit;
    private Integer viewsUsed;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static VaultItemShareResponse fromEntity(VaultItemShare share) {
        VaultItemShareResponse dto = new VaultItemShareResponse();
        dto.setShareId(share.getShareId());
        dto.setVaultItemId(share.getVaultItem().getVaultItemId());
        dto.setItemTitle(share.getVaultItem().getTitle());
        dto.setSharedByEmail(share.getSharedByUser().getEmail());
        dto.setSharedWithEmail(share.getSharedWithUser().getEmail());
        dto.setPermissionLevel(share.getPermissionLevel());
        dto.setViewCountLimit(share.getViewCountLimit());
        dto.setViewsUsed(share.getViewsUsed());
        dto.setExpiresAt(share.getExpiresAt());
        dto.setCreatedAt(share.getCreatedAt());
        return dto;
    }

    // Getters and Setters

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getVaultItemId() {
        return vaultItemId;
    }

    public void setVaultItemId(Long vaultItemId) {
        this.vaultItemId = vaultItemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getSharedByEmail() {
        return sharedByEmail;
    }

    public void setSharedByEmail(String sharedByEmail) {
        this.sharedByEmail = sharedByEmail;
    }

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

    public Integer getViewsUsed() {
        return viewsUsed;
    }

    public void setViewsUsed(Integer viewsUsed) {
        this.viewsUsed = viewsUsed;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
