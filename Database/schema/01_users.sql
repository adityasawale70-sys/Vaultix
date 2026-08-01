-- =====================================================
-- VAULTIX
-- Module : User Management
-- Version: 2.0 (Production-Ready)
-- =====================================================

USE vaultix_db;

-- Drop in dependency order (children first)
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_mfa;
DROP TABLE IF EXISTS password_history;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS users;

-- =====================================================
-- TABLE: users  (auth-only, kept lean & fast)
-- =====================================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Authentication
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    -- Per-user cryptographic salt (base64) used for client-side key derivation
    salt VARCHAR(128) NULL,

    -- Account Status
    account_status ENUM(
        'PENDING',
        'ACTIVE',
        'LOCKED',       -- automatic, due to failed logins
        'SUSPENDED',    -- manual, admin action
        'DELETED'
    ) NOT NULL DEFAULT 'PENDING',

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token_hash VARCHAR(255),
    email_verification_expires_at TIMESTAMP NULL,

    -- Security / login tracking
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45),
    last_failed_login_at TIMESTAMP NULL,
    account_locked_until TIMESTAMP NULL,

    -- Password reset (store hash, never raw token)
    password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    password_reset_token_hash VARCHAR(255),
    password_reset_expires_at TIMESTAMP NULL,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,          -- admin/inviter, nullable for self-signup

    -- Soft delete (single source of truth — no duplicate status flag)
    deleted_at TIMESTAMP NULL,

    CONSTRAINT chk_failed_attempts CHECK (failed_login_attempts >= 0),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by)
        REFERENCES users(user_id) ON DELETE SET NULL
);

-- Indexes for common query patterns
CREATE INDEX idx_users_status ON users(account_status);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login ON users(last_login_at);
CREATE INDEX idx_users_reset_token ON users(password_reset_token_hash);
CREATE INDEX idx_users_verify_token ON users(email_verification_token_hash);

-- =====================================================
-- TABLE: user_profiles  (display/personal data, split from auth)
-- =====================================================
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    display_name VARCHAR(100),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio VARCHAR(500),
    phone_number VARCHAR(20),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en-US',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_profile_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: roles  (RBAC)
-- =====================================================
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,   -- e.g. 'ADMIN', 'USER', 'MODERATOR'
    description VARCHAR(255)
);

INSERT INTO roles (role_name, description) VALUES
    ('ADMIN', 'Full system access'),
    ('MODERATOR', 'Content and user moderation'),
    ('USER', 'Standard user access');

-- =====================================================
-- TABLE: user_roles  (many-to-many)
-- =====================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NULL,

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_userroles_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_userroles_role FOREIGN KEY (role_id)
        REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_userroles_assigned_by FOREIGN KEY (assigned_by)
        REFERENCES users(user_id) ON DELETE SET NULL
);

-- =====================================================
-- TABLE: password_history  (prevent password reuse)
-- =====================================================
CREATE TABLE password_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pwhistory_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_pwhistory_user ON password_history(user_id, created_at);

-- =====================================================
-- TABLE: user_mfa  (2FA / TOTP support)
-- =====================================================
CREATE TABLE user_mfa (
    user_id BIGINT PRIMARY KEY,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    totp_secret_encrypted VARCHAR(255),      -- encrypted at rest, not plaintext
    backup_codes_hash JSON,                  -- array of hashed one-time codes
    mfa_enabled_at TIMESTAMP NULL,

    CONSTRAINT fk_mfa_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: refresh_tokens  (JWT refresh token rotation)
-- =====================================================
CREATE TABLE refresh_tokens (
    token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,  -- never store raw token
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,

    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expiry ON refresh_tokens(expires_at);

-- =====================================================
-- TABLE: user_sessions  (active session / "logout all devices")
-- =====================================================
CREATE TABLE user_sessions (
    session_id VARCHAR(64) PRIMARY KEY,      -- e.g. UUID
    user_id BIGINT NOT NULL,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,

    CONSTRAINT fk_session_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_sessions_expiry ON user_sessions(expires_at);