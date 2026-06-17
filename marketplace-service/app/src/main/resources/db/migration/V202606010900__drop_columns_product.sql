ALTER TABLE public.product
DROP COLUMN IF EXISTS installation_count,
DROP COLUMN IF EXISTS alternative_extension,
DROP COLUMN IF EXISTS is_maven_dropins,
DROP COLUMN IF EXISTS successor,
DROP COLUMN IF EXISTS is_focused,
DROP COLUMN IF EXISTS best_match_version,
DROP COLUMN IF EXISTS vendor_image_path,
DROP COLUMN IF EXISTS vendor_image_dark_mode_path,
DROP COLUMN IF EXISTS product_module_content,
DROP COLUMN IF EXISTS meta_product_json_url,
DROP COLUMN IF EXISTS compatibility_range;

ALTER TABLE public.artifact
DROP COLUMN IF EXISTS is_product_artifact;