package com.vaultix.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vault_item_shares")
public class VaultItemShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_item_id", nullable = false)
    private VaultItem vaultItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedByUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false)
    private PermissionLevel permissionLevel = PermissionLevel.READ;

    @Column(name = "view_count_limit")
    private Integer viewCountLimit;

    @Column(name = "views_used", nullable = false)
    private Integer viewsUsed = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public VaultItemShare() {
    }

    public VaultItemShare(VaultItem vaultItem, User sharedByUser, User sharedWithUser, PermissionLevel permissionLevel) {
        this.vaultItem = vaultItem;
        this.sharedByUser = sharedByUser;
        this.sharedWithUser = sharedWithUser;
        this.permissionLevel = permissionLevel != null ? permissionLevel : PermissionLevel.READ;
    }

    // Getters and Setters

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public VaultItem getVaultItem() {
        return vaultItem;
    }

    public void setVaultItem(VaultItem vaultItem) {
        this.vaultItem = vaultItem;
    }

    public User getSharedByUser() {
        return sharedByUser;
    }

    public void setSharedByUser(User sharedByUser) {
        this.sharedByUser = sharedByUser;
    }

    public User getSharedWithUser() {
        return sharedWithUser;
    }

    public void setSharedWithUser(User sharedWithUser) {
        this.sharedWithUser = sharedWithUser;
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
}
