package com.vaultix.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hash of the raw token. Raw token is returned to the client;
     * only the hash is persisted — so a DB breach does NOT expose live tokens.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private Instant expiry;

    /**
     * Stores the user's email (the JWT subject / principal identifier).
     */
    @Column(nullable = false, length = 255)
    private String username;

    @CreatedDate
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ─── Constructors ────────────────────────────────────────────────────────

    public RefreshToken() {}

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getTokenHash()             { return tokenHash; }
    public void setTokenHash(String h)       { this.tokenHash = h; }

    public boolean isRevoked()               { return revoked; }
    public void setRevoked(boolean revoked)  { this.revoked = revoked; }

    public Instant getExpiry()               { return expiry; }
    public void setExpiry(Instant expiry)    { this.expiry = expiry; }

    public String getUsername()              { return username; }
    public void setUsername(String username) { this.username = username; }

    public Instant getIssuedAt()             { return issuedAt; }
    public Instant getUpdatedAt()            { return updatedAt; }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public boolean isExpired() {
        return Instant.now().isAfter(expiry);
    }

    public boolean isActive() {
        return !revoked && !isExpired();
    }
}