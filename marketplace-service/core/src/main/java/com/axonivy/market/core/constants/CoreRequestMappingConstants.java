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
}
