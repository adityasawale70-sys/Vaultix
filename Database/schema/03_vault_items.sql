-- =====================================================
-- VAULTIX
-- Module : Vault Items Management (Zero-Knowledge Architecture)
-- Version: 1.0 (Production-Ready)
-- =====================================================

USE vaultix_db;

DROP TABLE IF EXISTS vault_item_tags;
DROP TABLE IF EXISTS vault_items;

-- =====================================================
-- TABLE: vault_items
-- Stores client-side encrypted secret payloads
-- =====================================================
CREATE TABLE vault_items (
    vault_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category ENUM(
        'CREDENTIAL',
        'SECURE_NOTE',
        'PAYMENT_CARD',
        'BANK_ACCOUNT',
        'API_KEY',
        'LICENSE'
    ) NOT NULL DEFAULT 'CREDENTIAL',
    title VARCHAR(150) NOT NULL,
    username_or_identifier VARCHAR(150),
    url VARCHAR(500),
    encrypted_payload TEXT NOT NULL,         -- Base64 encoded AES-GCM-256 ciphertext
    iv VARCHAR(64) NOT NULL,                 -- Base64 encoded Initialization Vector (12 bytes)
    auth_tag VARCHAR(64),                    -- Base64 encoded GCM Auth Tag (if separate)
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    is_trashed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_vault_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_vault_user ON vault_items(user_id);
CREATE INDEX idx_vault_category ON vault_items(user_id, category);
CREATE INDEX idx_vault_favorite ON vault_items(user_id, is_favorite);
CREATE INDEX idx_vault_trashed ON vault_items(user_id, is_trashed);

-- =====================================================
-- TABLE: vault_item_tags
-- Stores tags associated with vault items
-- =====================================================
CREATE TABLE vault_item_tags (
    vault_item_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,

    PRIMARY KEY (vault_item_id, tag_name),
    CONSTRAINT fk_tag_vault_item FOREIGN KEY (vault_item_id)
        REFERENCES vault_items(vault_item_id) ON DELETE CASCADE
);

CREATE INDEX idx_tags_name ON vault_item_tags(tag_name);
