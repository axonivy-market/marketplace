package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreRequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String IMAGE = API + "/image";
  public static final String BY_ID = "/{id}";
  public static final String SWAGGER_URL = "/swagger-ui/index.html";
  public static final String BEST_MATCH_BY_ID_AND_VERSION = "/{id}/{version}/bestmatch";
  public static final String PRODUCT_DETAILS = API + "/product-details";
  public static final String PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION = "/{id}/{version}/json";
}
