-- Add email and password hash to users; enforce unique email

ALTER TABLE tab_users 
    ADD COLUMN email VARCHAR(255) NOT NULL,
    ADD COLUMN password_hash VARCHAR(100) NOT NULL;

ALTER TABLE tab_users 
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Optional indices for lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON tab_users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON tab_users(username);
