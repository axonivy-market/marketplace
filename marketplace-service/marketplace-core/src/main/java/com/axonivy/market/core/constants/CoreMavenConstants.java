package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CoreMavenConstants {
  public static final String SNAPSHOT_VERSION = "SNAPSHOT";
  public static final String SNAPSHOT_RELEASE_POSTFIX = "-" + SNAPSHOT_VERSION;
  public static final String SPRINT_RELEASE_POSTFIX = "-m";
  public static final String PRODUCT_ARTIFACT_POSTFIX = "-product";
  public static final String MAIN_VERSION_REGEX = "\\.";
  public static final String TEST_ARTIFACTID = "-test";
  public static final String DEFAULT_IVY_MAVEN_BASE_URL = "https://maven.axonivy.com";
}

