ALTER TABLE public.passkey_credential
    ADD COLUMN IF NOT EXISTS credential_type VARCHAR(64),
    ADD COLUMN IF NOT EXISTS uv_initialized BOOLEAN,
    ADD COLUMN IF NOT EXISTS transports TEXT,
    ADD COLUMN IF NOT EXISTS backup_eligible BOOLEAN,
    ADD COLUMN IF NOT EXISTS backup_state BOOLEAN,
    ADD COLUMN IF NOT EXISTS attestation_object TEXT,
    ADD COLUMN IF NOT EXISTS attestation_client_data_json TEXT,
    ADD COLUMN IF NOT EXISTS label VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS last_used TIMESTAMP WITH TIME ZONE;

UPDATE public.passkey_credential
SET credential_type = COALESCE(credential_type, 'public-key'),
    uv_initialized = COALESCE(uv_initialized, TRUE),
    transports = COALESCE(transports, ''),
    backup_eligible = COALESCE(backup_eligible, FALSE),
    backup_state = COALESCE(backup_state, FALSE),
    attestation_object = COALESCE(attestation_object, ''),
    attestation_client_data_json = COALESCE(attestation_client_data_json, ''),
    label = COALESCE(label, 'Admin passkey'),
    created = COALESCE(created, CURRENT_TIMESTAMP),
    last_used = COALESCE(last_used, CURRENT_TIMESTAMP)
WHERE credential_type IS NULL
   OR uv_initialized IS NULL
   OR transports IS NULL
   OR backup_eligible IS NULL
   OR backup_state IS NULL
   OR attestation_object IS NULL
   OR attestation_client_data_json IS NULL
   OR label IS NULL
   OR created IS NULL
   OR last_used IS NULL;

ALTER TABLE public.passkey_credential
    ALTER COLUMN credential_type SET NOT NULL,
    ALTER COLUMN uv_initialized SET NOT NULL,
    ALTER COLUMN backup_eligible SET NOT NULL,
    ALTER COLUMN backup_state SET NOT NULL,
    ALTER COLUMN attestation_object SET NOT NULL,
    ALTER COLUMN attestation_client_data_json SET NOT NULL,
    ALTER COLUMN created SET NOT NULL;
