package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String SYNC = ROOT + "sync";
  public static final String USER_MAPPING = "/user";
  public static final String PRODUCT = API + "/product";
  public static final String PRODUCT_DETAILS = API + "/product-details";
  public static final String SWAGGER_URL = "/swagger-ui/index.html";
}