package com.axonivy.market.constants;

public class MavenConstants {
  public static final String SNAPSHOT_VERSION = "SNAPSHOT";
  public static final String SNAPSHOT_RELEASE_POSTFIX = "-" + SNAPSHOT_VERSION;
  public static final String SPRINT_RELEASE_POSTFIX = "-m";
  public static final String PRODUCT_ARTIFACT_POSTFIX = "-product";
  public static final String DEFAULT_IVY_MAVEN_BASE_URL = "https://maven.axonivy.com";
  public static final String ARTIFACT_FILE_NAME_FORMAT = "%s-%s.%s";
  public static final String ARTIFACT_NAME_FORMAT = "%s (%s)";
  public static final String VERSION_EXTRACT_FORMAT_FROM_METADATA_FILE = "//versions/version/text()";
  public static final String MAIN_VERSION_REGEX = "\\.";
  public static final String GROUP_ID_TAG = "groupId";
  public static final String ARTIFACT_ID_TAG = "artifactId";
  public static final String VERSIONING_TAG = "versioning";
  public static final String LATEST_VERSION_TAG = "latest";
  public static final String LATEST_RELEASE_TAG = "release";
  public static final String VERSIONS_LIST_TAG = "versions";
  public static final String VERSION_TAG = "version";
  public static final String lAST_UPDATED_TAG = "lastUpdated";

  private MavenConstants() {
  }
}
