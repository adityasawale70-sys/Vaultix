-- =====================================================
-- VAULTIX
-- Module : Folders, Secret Version History & Sharing ACL
-- Version: 1.0 (Enterprise Production-Ready)
-- =====================================================

USE vaultix_db;

DROP TABLE IF EXISTS vault_item_versions;
DROP TABLE IF EXISTS vault_item_shares;
DROP TABLE IF EXISTS vault_folders;

-- =====================================================
-- TABLE: vault_folders
-- Organizational folders/collections for vault items
-- =====================================================
CREATE TABLE vault_folders (
    folder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color_code VARCHAR(20) DEFAULT '#6366f1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_folder_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_folder_user ON vault_folders(user_id);

-- Add optional folder reference to vault_items table
ALTER TABLE vault_items
ADD COLUMN folder_id BIGINT NULL,
ADD CONSTRAINT fk_vault_folder FOREIGN KEY (folder_id)
    REFERENCES vault_folders(folder_id) ON DELETE SET NULL;

-- =====================================================
-- TABLE: vault_item_versions
-- Revision history for secrets (supports rollback)
-- =====================================================
CREATE TABLE vault_item_versions (
    version_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vault_item_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    encrypted_payload TEXT NOT NULL,
    iv VARCHAR(64) NOT NULL,
    auth_tag VARCHAR(64),
    changed_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_version_item FOREIGN KEY (vault_item_id)
        REFERENCES vault_items(vault_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_version_user FOREIGN KEY (changed_by_user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_version_item ON vault_item_versions(vault_item_id, version_number);

-- =====================================================
-- TABLE: vault_item_shares
-- Fine-grained secret sharing with permissions (ACL)
-- =====================================================
CREATE TABLE vault_item_shares (
    share_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vault_item_id BIGINT NOT NULL,
    shared_by_user_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    permission_level ENUM('READ', 'WRITE', 'ADMIN') NOT NULL DEFAULT 'READ',
    view_count_limit INT NULL,
    views_used INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_share_item FOREIGN KEY (vault_item_id)
        REFERENCES vault_items(vault_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_share_by_user FOREIGN KEY (shared_by_user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_share_with_user FOREIGN KEY (shared_with_user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_shares_with_user ON vault_item_shares(shared_with_user_id);
