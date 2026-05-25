-- =============================================
-- V1__init_schema.sql
-- Initial schema for Axon Ivy Marketplace
-- Generated for all JPA entities in core and app modules
-- =============================================

-- =============================================
-- CORE MODULE TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS product (
    id                             VARCHAR(255) NOT NULL PRIMARY KEY,
    market_directory               VARCHAR(255),
    tags                           TEXT         NOT NULL DEFAULT '',
    released_versions              TEXT         NOT NULL DEFAULT '',
    logo_url                       VARCHAR(255),
    listed                         BOOLEAN,
    deprecated                     BOOLEAN,
    type                           VARCHAR(255),
    vendor                         VARCHAR(255),
    vendor_url                     VARCHAR(255),
    version                        VARCHAR(255),
    vendor_image                   VARCHAR(255),
    vendor_image_dark_mode         VARCHAR(255),
    platform_review                VARCHAR(255),
    cost                           VARCHAR(255),
    repository_name                VARCHAR(255),
    source_url                     VARCHAR(255),
    status_badge_url               VARCHAR(255),
    language                       VARCHAR(255),
    industry                       VARCHAR(255),
    validate                       BOOLEAN,
    contact_us                     BOOLEAN,
    newest_published_date          TIMESTAMP,
    first_published_date           TIMESTAMP,
    newest_release_version         VARCHAR(255),
    synchronized_installation_count BOOLEAN,
    logo_id                        VARCHAR(255),
    created_at                     TIMESTAMP,
    updated_at                     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_name (
    product_id VARCHAR(255) NOT NULL REFERENCES product (id),
    language   VARCHAR(255) NOT NULL,
    name       TEXT,
    PRIMARY KEY (product_id, language)
);

CREATE TABLE IF NOT EXISTS product_description (
    product_id        VARCHAR(255) NOT NULL REFERENCES product (id),
    language          VARCHAR(255) NOT NULL,
    short_description TEXT,
    PRIMARY KEY (product_id, language)
);

CREATE TABLE IF NOT EXISTS archived_artifact (
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    last_version VARCHAR(255),
    group_id    VARCHAR(255),
    artifact_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS artifact (
    id                  VARCHAR(255) NOT NULL PRIMARY KEY,
    repo_url            VARCHAR(255),
    name                VARCHAR(255),
    group_id            VARCHAR(255),
    artifact_id         VARCHAR(255),
    type                VARCHAR(255),
    is_dependency       BOOLEAN,
    doc                 BOOLEAN,
    is_invalid_artifact BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS artifact_archived_artifacts (
    artifact_id           VARCHAR(255) NOT NULL REFERENCES artifact (id),
    archived_artifacts_id VARCHAR(255) NOT NULL REFERENCES archived_artifact (id),
    PRIMARY KEY (artifact_id, archived_artifacts_id)
);

CREATE TABLE IF NOT EXISTS product_artifacts (
    product_id   VARCHAR(255) NOT NULL REFERENCES product (id),
    artifacts_id VARCHAR(255) NOT NULL REFERENCES artifact (id),
    PRIMARY KEY (product_id, artifacts_id)
);

CREATE TABLE IF NOT EXISTS image (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id VARCHAR(255),
    image_url  VARCHAR(255),
    image_data BYTEA,
    sha        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS metadata (
    url                   VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id            VARCHAR(255),
    last_updated          TIMESTAMP,
    artifact_id           VARCHAR(255),
    group_id              VARCHAR(255),
    latest                VARCHAR(255),
    release               VARCHAR(255),
    versions              TEXT         NOT NULL DEFAULT '',
    repo_url              VARCHAR(255),
    type                  VARCHAR(255),
    name                  VARCHAR(255),
    is_product_artifact   BOOLEAN      NOT NULL DEFAULT FALSE,
    snapshot_version_value VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS product_custom_sort (
    rule_for_remainder VARCHAR(255) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS product_json_content (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    version    VARCHAR(255),
    product_id VARCHAR(255),
    name       VARCHAR(255),
    content    TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_module_content (
    id            VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id    VARCHAR(255),
    version       VARCHAR(255),
    is_dependency BOOLEAN,
    name          VARCHAR(255),
    group_id      VARCHAR(255),
    artifact_id   VARCHAR(255),
    type          VARCHAR(255),
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_module_content_description (
    product_module_content_id VARCHAR(255) NOT NULL REFERENCES product_module_content (id),
    language                  VARCHAR(255) NOT NULL,
    description               TEXT,
    PRIMARY KEY (product_module_content_id, language)
);

CREATE TABLE IF NOT EXISTS product_module_content_setup (
    product_module_content_id VARCHAR(255) NOT NULL REFERENCES product_module_content (id),
    language                  VARCHAR(255) NOT NULL,
    setup                     TEXT,
    PRIMARY KEY (product_module_content_id, language)
);

CREATE TABLE IF NOT EXISTS product_module_content_demo (
    product_module_content_id VARCHAR(255) NOT NULL REFERENCES product_module_content (id),
    language                  VARCHAR(255) NOT NULL,
    demo                      TEXT,
    PRIMARY KEY (product_module_content_id, language)
);

CREATE TABLE IF NOT EXISTS product_module_content_component (
    product_module_content_id VARCHAR(255) NOT NULL REFERENCES product_module_content (id),
    language                  VARCHAR(255) NOT NULL,
    component                 TEXT,
    PRIMARY KEY (product_module_content_id, language)
);

CREATE TABLE IF NOT EXISTS maven_artifact_version (
    artifact_id           VARCHAR(255) NOT NULL,
    product_version       VARCHAR(255) NOT NULL,
    is_additional_version BOOLEAN      NOT NULL DEFAULT FALSE,
    name                  VARCHAR(255),
    download_url          VARCHAR(255),
    is_invalid_artifact   BOOLEAN      NOT NULL DEFAULT FALSE,
    group_id              VARCHAR(255),
    product_id            VARCHAR(255),
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    PRIMARY KEY (artifact_id, product_version, is_additional_version)
);

CREATE TABLE IF NOT EXISTS product_marketplace_data (
    id                              VARCHAR(255) NOT NULL PRIMARY KEY,
    installation_count              INTEGER      NOT NULL DEFAULT 0,
    synchronized_installation_count BOOLEAN,
    custom_order                    INTEGER,
    successor                       VARCHAR(255),
    deprecation_date                TIMESTAMP,
    deprecation_requester           VARCHAR(255)
);

-- =============================================
-- APP MODULE TABLES
-- =============================================

CREATE TABLE IF NOT EXISTS github_user (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    git_hub_id VARCHAR(255),
    provider   VARCHAR(255),
    username   VARCHAR(255),
    name       VARCHAR(255),
    avatar_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS workflow_information (
    id                    VARCHAR(255) NOT NULL PRIMARY KEY,
    workflow_type         VARCHAR(255),
    last_built            TIMESTAMP,
    conclusion            VARCHAR(255),
    last_built_run_url    VARCHAR(255),
    current_workflow_state VARCHAR(255),
    disabled_date         TIMESTAMP,
    repository_id         VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS test_step (
    id            VARCHAR(255) NOT NULL PRIMARY KEY,
    name          VARCHAR(255),
    status        VARCHAR(255),
    type          VARCHAR(255),
    repository_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS github_repo (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    name       VARCHAR(255),
    product_id VARCHAR(255),
    html_url   VARCHAR(255),
    focused    BOOLEAN
);

CREATE TABLE IF NOT EXISTS sync_task (
    id             VARCHAR(255) NOT NULL PRIMARY KEY,
    type           VARCHAR(255),
    status         VARCHAR(255),
    last_run_date  TIMESTAMP,
    completed_date TIMESTAMP,
    message        TEXT,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_security_info (
    repo_name                VARCHAR(255) NOT NULL PRIMARY KEY,
    visibility               VARCHAR(255),
    branch_protection_enabled BOOLEAN     NOT NULL DEFAULT FALSE,
    last_commit_date         TIMESTAMP,
    latest_commit_sha        VARCHAR(255),
    dependabot_alerts        TEXT,
    dependabot_status        VARCHAR(255),
    code_scanning_alerts     TEXT,
    code_scanning_status     VARCHAR(255),
    secret_scanning_status   VARCHAR(255),
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP
);

CREATE TABLE IF NOT EXISTS release_letter (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    sprint     VARCHAR(255),
    content    TEXT,
    is_latest  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS release_letter_drafts (
    id                 VARCHAR(255) NOT NULL PRIMARY KEY,
    git_hub_user_id    VARCHAR(255),
    release_letter_id  VARCHAR(255),
    draft_content      TEXT,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_dependency (
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id   VARCHAR(255),
    artifact_id  VARCHAR(255),
    version      VARCHAR(255),
    download_url VARCHAR(255),
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_dependency_dependencies (
    product_dependency_id VARCHAR(255) NOT NULL REFERENCES product_dependency (id),
    dependencies_id       VARCHAR(255) NOT NULL REFERENCES product_dependency (id),
    PRIMARY KEY (product_dependency_id, dependencies_id)
);

CREATE TABLE IF NOT EXISTS external_document_meta (
    id                VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id        VARCHAR(255),
    artifact_id       VARCHAR(255),
    artifact_name     VARCHAR(255),
    version           VARCHAR(255),
    storage_directory VARCHAR(255),
    relative_link     VARCHAR(255),
    language          VARCHAR(255),
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_designer_installation (
    id                 VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id         VARCHAR(255),
    designer_version   VARCHAR(255),
    installation_count INTEGER      NOT NULL DEFAULT 0,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP
);

CREATE TABLE IF NOT EXISTS github_repo_meta (
    repo_url   VARCHAR(255) NOT NULL PRIMARY KEY,
    repo_name  VARCHAR(255),
    last_change BIGINT,
    last_sha1  VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS feedback (
    id              VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id         VARCHAR(255),
    product_id      VARCHAR(255),
    content         TEXT,
    rating          INTEGER,
    feedback_status VARCHAR(255),
    moderator_name  VARCHAR(255),
    review_date     TIMESTAMP,
    version         INTEGER,
    is_latest       BOOLEAN,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);
