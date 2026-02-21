-- Add direct user reference to tasks for faster authorization/filtering
ALTER TABLE tab_tasks
    ADD COLUMN IF NOT EXISTS user_id UUID;

-- Backfill user_id from linked dharma
UPDATE tab_tasks t
SET user_id = d.user_id
FROM tab_dharma d
WHERE t.dharma_id = d.id
  AND t.user_id IS NULL;

-- Enforce NOT NULL and FK after backfill
ALTER TABLE tab_tasks
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE tab_tasks
    ADD CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
            REFERENCES tab_users(id)
            ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tab_tasks(user_id);
