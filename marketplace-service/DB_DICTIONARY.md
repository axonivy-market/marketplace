# Marketplace BE Database Dictionary

Scope: `marketplace-service/app`, `marketplace-service/core`, and `marketplace-service/stable`.

`stable` does not define its own JPA tables. It boots the `core` entities and repositories.

## Core Module

| Table | Entity class | Use                                                                                                                     |
|---|---|-------------------------------------------------------------------------------------------------------------------------|
| `app_settings` | `com.axonivy.market.core.entity.AppSetting` | Runtime application settings and feature flags.                                                                         |
| `archived_artifact` | `com.axonivy.market.core.entity.ArchivedArtifact` | Stores archived artifact information (group id and artifact id for version sync).                                       |
| `artifact` | `com.axonivy.market.core.entity.Artifact` | Canonical artifact record for a marketplace product.                                                                    |
| `image` | `com.axonivy.market.core.entity.Image` | Product image/logo cache, including binary image data.                                                                  |
| `maven_artifact_version` | `com.axonivy.market.core.entity.MavenArtifactVersion` | List of available artifacts (and download url) base on each specific version of products.                               |
| `metadata` | `com.axonivy.market.core.entity.Metadata` | Maven metadata cache for product/artifact version sync. This one will provide url of artifact metadata, synced version. |
| `product` | `com.axonivy.market.core.entity.Product` | Main product item records for the marketplace.                                                                          |
| `product_custom_sort` | `com.axonivy.market.core.entity.ProductCustomSort` | Controls custom ordering for product listing in standard sorting mode.                                                  |
| `product_json_content` | `com.axonivy.market.core.entity.ProductJsonContent` | Cached JSON content for a product version.                                                                              |
| `product_marketplace_data` | `com.axonivy.market.core.entity.ProductMarketplaceData` | Marketplace-only product stats and lifecycle metadata.                                                                  |
| `product_module_content` | `com.axonivy.market.core.entity.ProductModuleContent` | Localized product readme which will be shown in the product detail section.                                             |

## App Module

| Table | Entity class | Use                                                                                                            |
|---|---|----------------------------------------------------------------------------------------------------------------|
| `external_document_meta` | `com.axonivy.market.entity.ExternalDocumentMeta` | Metadata for external product documents. (version, relativelink and corresponding document storage directory   |
| `feedback` | `com.axonivy.market.entity.Feedback` | User feedback, rating, moderation, and review state.                                                           |
| `github_repo` | `com.axonivy.market.entity.GithubRepo` | GitHub repository linked to a product (used to display monitoring information).                                |
| `github_repo_meta` | `com.axonivy.market.entity.GitHubRepoMeta` | Cached GitHub repository sync metadata (repo name, directory path of items metadata, latest change time, sha). |
| `github_user` | `com.axonivy.market.entity.GithubUser` | GitHub-authenticated user identity.                                                                            |
| `product_dependency` | `com.axonivy.market.entity.ProductDependency` | Product dependency graph and dependency version data.                                                          |
| `product_designer_installation` | `com.axonivy.market.entity.ProductDesignerInstallation` | Installation counts per product and Designer version.                                                          |
| `product_security_info` | `com.axonivy.market.entity.ProductSecurityInfo` | GitHub security monitoring data for a repository.                                                              |
| `release_letter` | `com.axonivy.market.entity.ReleaseLetter` | Release note content for a sprint or release.                                                                  |
| `release_letter_drafts` | `com.axonivy.market.entity.ReleaseLetterDraft` | Draft text for release letters.                                                                                |
| `sync_task` | `com.axonivy.market.entity.SyncTaskExecution` | Sync job execution status and history.                                                                         |
| `test_step` | `com.axonivy.market.entity.TestStep` | Workflow test step (CI/DEV/E2E build) result and status.                                                       |
| `workflow_information` | `com.axonivy.market.entity.WorkflowInformation` | Workflow run summary for a repository.                                                                         |

## Join Tables And Collection Tables

| Table | Owned by | Use |
|---|---|---|
| `artifact_archived_artifacts` | `Artifact.archivedArtifacts` | Links an artifact to its archived versions. |
| `product_artifacts` | `Product.artifacts` | Links products to artifact records. |
| `product_dependency_dependencies` | `ProductDependency.dependencies` | Self-referencing dependency links between product dependencies. |
| `product_name` | `Product.names` | Localized product name values. |
| `product_description` | `Product.shortDescriptions` | Localized product short description values. |
| `product_module_content_description` | `ProductModuleContent.description` | Localized module description content. |
| `product_module_content_setup` | `ProductModuleContent.setup` | Localized setup tab content. |
| `product_module_content_demo` | `ProductModuleContent.demo` | Localized demo tab content. |
| `product_module_content_component` | `ProductModuleContent.component` | Localized component tab content. |

## Migrations

### Base schema

`marketplace-service/app/src/main/resources/db/migration/V1__init_schema.sql` creates the initial schema for the tables above.

### Later schema changes

| Migration | Change |
|---|---|
| `V202606010900__drop_columns_product.sql` | Removes obsolete columns from `product` and `artifact`. |
| `V202606120900__add_alternative_extension_and_logo_dark_id.sql` | Adds `product_marketplace_data.alternative_extension` and `product.logo_dark_id`. |
| `V202606160900__add_is_archived_field.sql` | Adds `product.is_archived`. |
| `V202606240900__update_sync_version.sql` | Adds and backfills `sync_task.version`. |
| `V202606250900__create_app_settings_table.sql` | Creates `app_settings`. |
| `V202607030900__add_node_number_column.sql` | Adds `sync_task.node_number`. |
| `V202607090900__add_vendor_logo_fields.sql` | Adds `product.vendor_logo`, `product.vendor_logo_dark_mode`, and `product.internal`. |
| `V202607240600__marp_4424_change_productid_and_category.sql` | Data migration that renames product IDs and updates related rows. |
| `V202607291134__marp_4737_clean-up-monitoring-data.sql` | Truncates monitoring tables after the product/category reshuffle. |

## Notes

| Note | Details |
|---|---|
| `stable` module | Reuses `core` entities and repositories; it does not define new persistent tables. |
| Transient fields | Several `Product` fields are transient and are not stored in the `product` table. |
| DDL alignment | `feedback` is worth a second look if you want the DDL and entity model aligned perfectly, because the entity inherits `id` from `AuditableIdEntity`. |

## Required Constraint Script

Run the following migration script to add the missing `product_id -> product(id)` foreign keys for product-linked tables:

```sql
-- Add missing foreign keys from product-linked tables to product(id).
-- Tables already constrained in V1 are intentionally excluded.

ALTER TABLE public.external_document_meta
  ADD CONSTRAINT fk_external_document_meta_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.feedback
  ADD CONSTRAINT fk_feedback_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.github_repo
  ADD CONSTRAINT fk_github_repo_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.image
  ADD CONSTRAINT fk_image_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.maven_artifact_version
  ADD CONSTRAINT fk_maven_artifact_version_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.metadata
  ADD CONSTRAINT fk_metadata_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.product_dependency
  ADD CONSTRAINT fk_product_dependency_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.product_designer_installation
  ADD CONSTRAINT fk_product_designer_installation_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.product_json_content
  ADD CONSTRAINT fk_product_json_content_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);

ALTER TABLE public.product_module_content
  ADD CONSTRAINT fk_product_module_content_product_id
  FOREIGN KEY (product_id) REFERENCES public.product (id);
```

This script needs to be run on the database because these tables currently store `product_id` as a plain column without a foreign key in the base schema.
