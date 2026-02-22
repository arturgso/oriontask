ALTER TABLE tab_users
ADD COLUMN is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN confirmation_token VARCHAR(255),
ADD COLUMN confirmation_token_expires_at TIMESTAMP;
