ALTER TABLE public.sync_task
ADD COLUMN IF NOT EXISTS version INTEGER;

UPDATE public.sync_task
SET version = 0
WHERE version IS NULL;

ALTER TABLE public.sync_task
ALTER COLUMN version SET DEFAULT 0;

ALTER TABLE public.sync_task
ALTER COLUMN version SET NOT NULL;
