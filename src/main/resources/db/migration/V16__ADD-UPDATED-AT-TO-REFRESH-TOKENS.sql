ALTER TABLE tab_refresh_tokens
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_updated_at
    ON tab_refresh_tokens(updated_at);
