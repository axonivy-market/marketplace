CREATE TABLE IF NOT EXISTS public.app_settings (
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    category    VARCHAR(255),
    description VARCHAR(255),
    encrypted   BOOLEAN,
    key         VARCHAR(255) NOT NULL,
    value       VARCHAR(255)
);