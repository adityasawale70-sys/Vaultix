-- =====================================================
-- VAULTIX
-- Module : Audit Logging & Security Tracking
-- Version: 1.0 (Production-Ready)
-- =====================================================

USE vaultix_db;

DROP TABLE IF EXISTS audit_logs;

-- =====================================================
-- TABLE: audit_logs
-- Records security and vault operation activities
-- =====================================================
CREATE TABLE audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,                      -- Nullable for failed logins or unauthenticated events
    event_type VARCHAR(50) NOT NULL,          -- e.g., 'LOGIN_SUCCESS', 'VAULT_ITEM_CREATE', 'PASSWORD_CHANGE'
    description VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_event ON audit_logs(event_type);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
