-- Refresh Tokens Table for Vaultix Authentication
-- Generated: 2025-09-22
-- Stores JWT refresh tokens with revocation support

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    expiry DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index for faster lookup by token_hash
CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);

-- Index for faster lookup by username
CREATE INDEX idx_refresh_token_username ON refresh_tokens(username);

-- Optional: foreign key if storing additional metadata
-- ALTER TABLE refresh_tokens ADD COLUMN user_id BIGINT;
-- ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(user_id);