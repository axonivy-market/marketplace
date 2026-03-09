package com.axonivy.market.stable.constants;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.API;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String PRODUCT = API + "/product";
  public static final String PRODUCT_DETAILS = API + "/product-details";
  public static final String VERSIONS_BY_ID = "/{id}/versions";
  public static final String PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION = "/{id}/install";
  public static final String BEST_MATCH_BY_ID_AND_VERSION = "/{id}/{version}/bestmatch";
}
