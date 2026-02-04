package com.axonivy.market.stable.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String PRODUCT = API + "/product";
  public static final String VERSIONS_BY_ID = "/{id}/versions";
  public static final String PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION = "/{id}/install";
}
