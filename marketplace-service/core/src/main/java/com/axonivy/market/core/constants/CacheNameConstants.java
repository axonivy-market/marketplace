package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Cache name constants defining cache configuration keys for caching repository data and responses.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheNameConstants {
  public static final String REPO_RELEASES = "RepoReleases";
  public static final String FIND_PRODUCTS = "FindProducts";
  public static final String FIND_PRODUCT_BY_ID_VERSION = "FindProductByIdAndVersion";
  public static final String FIND_PRODUCT_BY_ID_STATE = "FindProductByIdAndState";
  public static final String FIND_IMAGE = "FindImage";
  public static final String GET_GITHUB_RELEASES = "GetGitHubReleases";
  public static final String GET_GITHUB_RELEASES_PRODUCT_ID = "GetGitHubReleasesByProductIdAndReleaseId";
  public static final String APP_SETTINGS_FIND_ALL = "AppSettingsFindAll";
  public static final String APP_SETTINGS_SEARCH = "AppSettingsSearch";
  public static final String APP_SETTINGS_GET_BY_CATEGORY = "AppSettingsGetByCategory";
  public static final String APP_SETTINGS_GET_STRING_VALUE = "AppSettingsGetStringValue";
  public static final String APP_SETTINGS_GET_LONG_VALUE = "AppSettingsGetLongValue";
  public static final String APP_SETTINGS_GET_INTEGER_VALUE = "AppSettingsGetIntegerValue";
  public static final String APP_SETTINGS_GET_BOOLEAN_VALUE = "AppSettingsGetBooleanValue";
}
