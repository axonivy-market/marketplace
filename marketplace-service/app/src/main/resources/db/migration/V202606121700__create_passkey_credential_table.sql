CREATE TABLE IF NOT EXISTS public.passkey_credential (
    id              VARCHAR(255) NOT NULL PRIMARY KEY,
    github_user_id  VARCHAR(255) NOT NULL UNIQUE REFERENCES public.github_user (id) ON DELETE CASCADE,
    credential_id   VARCHAR(1024) NOT NULL UNIQUE,
    user_handle     VARCHAR(1024) NOT NULL,
    public_key_cose TEXT NOT NULL,
    signature_count BIGINT NOT NULL
);
