package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Core request mapping constants defining base API endpoint paths and route prefixes used across the Core module.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreRequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String IMAGE = API + "/image";
  public static final String BY_ID = "/{id}";
  public static final String SWAGGER_URL = "/swagger-ui/index.html";
  public static final String BEST_MATCH_VERSION_BY_ID_AND_VERSION = "/{id}/{version}/best-match-version";
}
