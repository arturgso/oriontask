-- Remove username from users table (email becomes the unique login identifier)
DROP INDEX IF EXISTS idx_users_username;

ALTER TABLE tab_users
    DROP CONSTRAINT IF EXISTS tab_users_username_key,
    DROP COLUMN IF EXISTS username;
