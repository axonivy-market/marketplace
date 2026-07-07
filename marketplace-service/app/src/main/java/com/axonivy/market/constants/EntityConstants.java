package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Entity table name constants defining database table names for JPA entity mapping.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityConstants {
  public static final String GITHUB_USER = "github_user";
  public static final String PRODUCT_DESIGNER_INSTALLATION = "product_designer_installation";
  public static final String GH_REPO_META = "github_repo_meta";
  public static final String FEEDBACK = "feedback";
  public static final String EXTERNAL_DOCUMENT_META = "external_document_meta";
  public static final String PRODUCT_DEPENDENCY = "product_dependency";
  public static final String REPOSITORY_ID = "repository_id";
  public static final String GITHUB_REPO = "github_repo";
  public static final String TEST_STEP = "test_step";
  public static final String WORKFLOW_INFORMATION = "workflow_information";
  public static final String SYNC_TASK = "sync_task";
  public static final String RELEASE_LETTER = "release_letter";
  public static final String PRODUCT_SECURITY_INFO = "product_security_info";
  public static final String RELEASE_LETTER_DRAFTS = "release_letter_drafts";
  public static final String APP_SETTINGS = "app_settings";
}
