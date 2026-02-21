UPDATE tab_tasks
SET status = 'WAITING'
WHERE status = 'NEXT';

ALTER TABLE tab_tasks
ALTER COLUMN status SET DEFAULT 'WAITING';
