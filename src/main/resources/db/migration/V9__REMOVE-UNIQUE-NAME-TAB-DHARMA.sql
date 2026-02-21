ALTER TABLE tab_dharma
    DROP CONSTRAINT IF EXISTS tab_dharma_name_key;

CREATE INDEX IF NOT EXISTS idx_tab_dharma_name ON tab_dharma(name);