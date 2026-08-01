-- =====================================================
-- VAULTIX
-- Module : Authentication – Refresh Tokens
-- Version: 2.0 (Production-Ready)
-- Updated: 2026-07-30
-- =====================================================

USE vaultix_db;

DROP TABLE IF EXISTS refresh_tokens;

-- =====================================================
-- TABLE: refresh_tokens
--
-- Security design:
--   • Only the SHA-256 hash of the raw token is stored.
--   • The raw token is returned to the client and NEVER persisted.
--   • A DB breach therefore does NOT expose live sessions.
--   • One active token per user (previous tokens purged on new login).
-- =====================================================
CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- SHA-256 hex hash of the raw UUID-based token
    token_hash  VARCHAR(255)    NOT NULL UNIQUE,

    -- User identifier (email / JWT subject)
    username    VARCHAR(255)    NOT NULL,

    -- Revocation flag — set to TRUE on logout or token rotation
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Token lifetime
    expiry      TIMESTAMP       NOT NULL,

    -- Audit columns (populated by Spring Data @EntityListeners)
    issued_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_user FOREIGN KEY (username)
        REFERENCES users(email) ON DELETE CASCADE
);

-- ─── Indexes ──────────────────────────────────────────────────────────────────
-- Primary lookup: validate incoming token
CREATE INDEX idx_refresh_token_hash     ON refresh_tokens(token_hash);
-- Lookup all tokens for a user (used by logout-all-devices)
CREATE INDEX idx_refresh_username       ON refresh_tokens(username);
-- Cleanup queries: find expired tokens
CREATE INDEX idx_refresh_expiry         ON refresh_tokens(expiry);