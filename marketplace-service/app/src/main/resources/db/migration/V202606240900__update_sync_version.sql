ALTER TABLE sync_task
ADD COLUMN IF NOT EXISTS version integer;

UPDATE sync_task
SET version = 0
WHERE version IS NULL;