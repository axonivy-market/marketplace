ALTER TABLE sync_task
ADD COLUMN IF NOT EXISTS node_number integer DEFAULT 1;

UPDATE sync_task
SET node_number = 1
WHERE node_number IS NULL;