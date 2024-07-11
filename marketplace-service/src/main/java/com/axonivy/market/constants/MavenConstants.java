package com.axonivy.market.constants;

public class MavenConstants {
  private MavenConstants() {}

  public static final String SNAPSHOT_RELEASE_POSTFIX = "-SNAPSHOT";
  public static final String SPRINT_RELEASE_POSTFIX = "-m";
  public static final String PRODUCT_ARTIFACT_POSTFIX = "-product";
  public static final String METADATA_URL_FORMAT = "%s/%s/%s/maven-metadata.xml";
  public static final String DEFAULT_IVY_MAVEN_BASE_URL = "https://maven.axonivy.com";
  public static final String ARTIFACT_DOWNLOAD_URL_FORMAT = "%s/%s/%s/%s/%s-%s.%s";
  public static final String ARTIFACT_NAME_FORMAT = "%s (%s)";
  public static final String VERSION_EXTRACT_FORMAT_FROM_METADATA_FILE = "//versions/version/text()";
}
