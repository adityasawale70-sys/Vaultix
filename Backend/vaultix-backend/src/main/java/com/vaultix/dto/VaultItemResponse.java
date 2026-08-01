package com.vaultix.dto;

import com.vaultix.entity.VaultCategory;
import com.vaultix.entity.VaultItem;

import java.time.LocalDateTime;
import java.util.Set;

public class VaultItemResponse {

    private Long vaultItemId;
    private VaultCategory category;
    private String title;
    private String usernameOrIdentifier;
    private String url;
    private String encryptedPayload;
    private String iv;
    private String authTag;
    private Boolean isFavorite;
    private Boolean isTrashed;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VaultItemResponse fromEntity(VaultItem item) {
        VaultItemResponse res = new VaultItemResponse();
        res.setVaultItemId(item.getVaultItemId());
        res.setCategory(item.getCategory());
        res.setTitle(item.getTitle());
        res.setUsernameOrIdentifier(item.getUsernameOrIdentifier());
        res.setUrl(item.getUrl());
        res.setEncryptedPayload(item.getEncryptedPayload());
        res.setIv(item.getIv());
        res.setAuthTag(item.getAuthTag());
        res.setIsFavorite(item.getIsFavorite());
        res.setIsTrashed(item.getIsTrashed());
        res.setTags(item.getTags());
        res.setCreatedAt(item.getCreatedAt());
        res.setUpdatedAt(item.getUpdatedAt());
        return res;
    }

    // Getters and Setters

    public Long getVaultItemId() {
        return vaultItemId;
    }

    public void setVaultItemId(Long vaultItemId) {
        this.vaultItemId = vaultItemId;
    }

    public VaultCategory getCategory() {
        return category;
    }

    public void setCategory(VaultCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsernameOrIdentifier() {
        return usernameOrIdentifier;
    }

    public void setUsernameOrIdentifier(String usernameOrIdentifier) {
        this.usernameOrIdentifier = usernameOrIdentifier;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public Boolean getIsTrashed() {
        return isTrashed;
    }

    public void setIsTrashed(Boolean trashed) {
        isTrashed = trashed;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
