package com.vaultix.dto;

import com.vaultix.entity.VaultItemVersion;

import java.time.LocalDateTime;

public class VaultItemVersionResponse {

    private Long versionId;
    private Long vaultItemId;
    private Integer versionNumber;
    private String encryptedPayload;
    private String iv;
    private String authTag;
    private String changedByEmail;
    private LocalDateTime createdAt;

    public static VaultItemVersionResponse fromEntity(VaultItemVersion v) {
        VaultItemVersionResponse dto = new VaultItemVersionResponse();
        dto.setVersionId(v.getVersionId());
        dto.setVaultItemId(v.getVaultItem().getVaultItemId());
        dto.setVersionNumber(v.getVersionNumber());
        dto.setEncryptedPayload(v.getEncryptedPayload());
        dto.setIv(v.getIv());
        dto.setAuthTag(v.getAuthTag());
        dto.setChangedByEmail(v.getChangedByUser().getEmail());
        dto.setCreatedAt(v.getCreatedAt());
        return dto;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getVaultItemId() {
        return vaultItemId;
    }

    public void setVaultItemId(Long vaultItemId) {
        this.vaultItemId = vaultItemId;
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

    public String getChangedByEmail() {
        return changedByEmail;
    }

    public void setChangedByEmail(String changedByEmail) {
        this.changedByEmail = changedByEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
