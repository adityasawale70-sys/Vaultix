package com.vaultix.dto;

import com.vaultix.entity.VaultCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class VaultItemRequest {

    @NotNull(message = "Category is required")
    private VaultCategory category;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    private String usernameOrIdentifier;

    private String url;

    @NotBlank(message = "Encrypted payload is required")
    private String encryptedPayload;

    @NotBlank(message = "IV is required")
    private String iv;

    private String authTag;

    private Boolean isFavorite = false;

    private Set<String> tags;

    private Long folderId;

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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
}
