-- Add email and password hash to users; enforce unique email

-- 1) Add columns as NULLABLE to allow backfilling
ALTER TABLE tab_users 
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN password_hash VARCHAR(100);

-- 2) Backfill existing users with placeholder email based on username
UPDATE tab_users 
SET email = username || '@local.invalid'
WHERE email IS NULL;

-- 3) Create pgcrypto extension (for bcrypt) and set default password hash
-- Default password: ChangeMe123! (meets policy: upper, lower, number, special)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
UPDATE tab_users 
SET password_hash = crypt('ChangeMe123!', gen_salt('bf'))
WHERE password_hash IS NULL;

-- 4) Enforce NOT NULL and uniqueness after backfill
ALTER TABLE tab_users 
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password_hash SET NOT NULL;

ALTER TABLE tab_users 
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- 5) Optional indices for lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON tab_users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON tab_users(username);
