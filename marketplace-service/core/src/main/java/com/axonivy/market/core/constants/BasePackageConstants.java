package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Base package constants defining package names for Spring component scanning across Core, App, and Stable modules.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BasePackageConstants {
  public static final String CORE_BASE_PACKAGE_NAME = "com.axonivy.market.core";
  public static final String APP_PACKAGE_NAME = "com.axonivy.market";
  public static final String STABLE_PACKAGE_NAME = "com.axonivy.market.stable";
  public static final String CORE_BASE_PACKAGE_REPO_NAME = CORE_BASE_PACKAGE_NAME + ".repository";
  public static final String CORE_BASE_PACKAGE_ENTITY_NAME = CORE_BASE_PACKAGE_NAME + ".entity";
}