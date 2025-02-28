package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenConstants {
  public static final String SNAPSHOT_VERSION = "SNAPSHOT";
  public static final String SNAPSHOT_RELEASE_POSTFIX = "-" + SNAPSHOT_VERSION;
  public static final String SPRINT_RELEASE_POSTFIX = "-m";
  public static final String DEV_RELEASE_POSTFIX = "-dev";
  public static final String PRODUCT_ARTIFACT_POSTFIX = "-product";
  public static final String DEFAULT_IVY_MAVEN_BASE_URL = "https://maven.axonivy.com";
  public static final String ARTIFACT_FILE_NAME_FORMAT = "%s-%s.%s";
  public static final String ARTIFACT_NAME_FORMAT = "%s (%s)";
  public static final String MAIN_VERSION_REGEX = "\\.";
  public static final String LATEST_VERSION_TAG = "latest";
  public static final String LATEST_RELEASE_TAG = "release";
  public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
  public static final String VERSION_TAG = "version";
  public static final String LAST_UPDATED_TAG = "lastUpdated";
  public static final String METADATA_URL_POSTFIX = "maven-metadata.xml";
  public static final String SNAPSHOT_LAST_UPDATED_TAG = "timestamp";
  public static final String SNAPSHOT_LAST_UPDATED_DATE_TIME_FORMAT = "yyyyMMdd.HHmmss";
  public static final String VALUE_TAG = "value";
  public static final String DEFAULT_PRODUCT_FOLDER_TYPE = "zip";
  public static final String APP_ZIP_FORMAT = "%s-app-%s.zip";
  public static final String POM = "pom.xml";
  public static final String DOC = "-doc";
}
