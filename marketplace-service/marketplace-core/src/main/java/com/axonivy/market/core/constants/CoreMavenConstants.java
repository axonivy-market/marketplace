package com.axonivy.market.core.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CoreMavenConstants {
  public static final String SNAPSHOT_VERSION = "SNAPSHOT";
  public static final String SNAPSHOT_RELEASE_POSTFIX = "-" + SNAPSHOT_VERSION;
  public static final String SPRINT_RELEASE_POSTFIX = "-m";
  public static final String PRODUCT_ARTIFACT_POSTFIX = "-product";
  public static final String MAIN_VERSION_REGEX = "\\.";
  public static final String TEST_ARTIFACT_ID = "-test";
  public static final String DEFAULT_IVY_MAVEN_BASE_URL = "https://maven.axonivy.com";
  public static final String DEV_RELEASE_POSTFIX = "-dev";
  public static final String DEV_RELEASE_PREFIX = "dev-";
}

