package com.vaultix.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vault_item_versions")
public class VaultItemVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long versionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_item_id", nullable = false)
    private VaultItem vaultItem;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT")
    private String encryptedPayload;

    @Column(nullable = false, length = 64)
    private String iv;

    @Column(name = "auth_tag", length = 64)
    private String authTag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedByUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public VaultItemVersion() {
    }

    public VaultItemVersion(VaultItem vaultItem, Integer versionNumber, String encryptedPayload, String iv, String authTag, User changedByUser) {
        this.vaultItem = vaultItem;
        this.versionNumber = versionNumber;
        this.encryptedPayload = encryptedPayload;
        this.iv = iv;
        this.authTag = authTag;
        this.changedByUser = changedByUser;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public VaultItem getVaultItem() {
        return vaultItem;
    }

    public void setVaultItem(VaultItem vaultItem) {
        this.vaultItem = vaultItem;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getAuthTag() {
        return authTag;
    }

    public void setAuthTag(String authTag) {
        this.authTag = authTag;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
