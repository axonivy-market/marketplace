ALTER TABLE public.product_marketplace_data
ADD COLUMN IF NOT EXISTS alternative_extension VARCHAR(255);

ALTER TABLE public.product
ADD COLUMN IF NOT EXISTS logo_dark_id VARCHAR(255);