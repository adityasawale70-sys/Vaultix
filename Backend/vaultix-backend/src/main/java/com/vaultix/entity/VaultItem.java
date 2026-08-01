package com.vaultix.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vault_items")
public class VaultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vault_item_id")
    private Long vaultItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaultCategory category = VaultCategory.CREDENTIAL;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "username_or_identifier", length = 150)
    private String usernameOrIdentifier;

    @Column(length = 500)
    private String url;

    @Column(name = "encrypted_payload", nullable = false, columnDefinition = "TEXT")
    private String encryptedPayload;

    @Column(nullable = false, length = 64)
    private String iv;

    @Column(name = "auth_tag", length = 64)
    private String authTag;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "is_trashed", nullable = false)
    private Boolean isTrashed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private VaultFolder folder;

    @ElementCollection
    @CollectionTable(name = "vault_item_tags", joinColumns = @JoinColumn(name = "vault_item_id"))
    @Column(name = "tag_name")
    private Set<String> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public VaultItem() {
    }

    // Getters and Setters

    public Long getVaultItemId() {
        return vaultItemId;
    }

    public void setVaultItemId(Long vaultItemId) {
        this.vaultItemId = vaultItemId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public VaultFolder getFolder() {
        return folder;
    }

    public void setFolder(VaultFolder folder) {
        this.folder = folder;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
