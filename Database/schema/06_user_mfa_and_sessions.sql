-- =====================================================
-- VAULTIX
-- Module : Multi-Factor Authentication (TOTP 2FA) & Devices
-- Version: 1.0 (Enterprise Production-Ready)
-- =====================================================

USE vaultix_db;

DROP TABLE IF EXISTS user_mfa_details;

-- =====================================================
-- TABLE: user_mfa_details
-- Stores encrypted TOTP secrets & hashed emergency backup codes
-- =====================================================
CREATE TABLE user_mfa_details (
    mfa_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    totp_secret_encrypted VARCHAR(255) NOT NULL,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    backup_codes_json JSON,
    mfa_enabled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mfadetails_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_mfa_user ON user_mfa_details(user_id);
