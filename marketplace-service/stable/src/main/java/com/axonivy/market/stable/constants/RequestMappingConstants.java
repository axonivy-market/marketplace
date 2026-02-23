package com.axonivy.market.stable.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.API;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMappingConstants {
  public static final String PRODUCT = API + "/product";
  public static final String VERSIONS_BY_ID = "/{id}/versions";
  public static final String PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION = "/{id}/install";
}
