package com.axonivy.market.neo.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String PRODUCT = API + "/product";
  public static final String PRODUCT_DETAILS = API + "/product-details";
  public static final String PRODUCT_DESIGNER_INSTALLATION = API + "/product-designer-installation";
  public static final String FEEDBACK = API + "/feedback";
  public static final String IMAGE = API + "/image";
  public static final String SYNC = "sync";
  public static final String SYNC_FIRST_PUBLISHED_DATE_ALL_PRODUCTS = SYNC + "/first-published-date";
  public static final String SYNC_ONE_PRODUCT_BY_ID = "sync/{id}";
  public static final String SWAGGER_URL = "/swagger-ui/index.html";
  public static final String GITHUB_LOGIN = "/github/login";
  public static final String GITHUB_REQUEST_ACCESS = "/github/request-access";
  public static final String GITHUB_VALIDATE_TOKEN = "/github/validate-token";
  public static final String AUTH = "/auth";
  public static final String BY_ID = "/{id}";
  public static final String BY_FILE_NAME = "/preview/{imageName}";
  public static final String BY_ID_AND_VERSION = "/{id}/{version}";
  public static final String BEST_MATCH_BY_ID_AND_VERSION = "/{id}/{version}/bestmatch";
  public static final String BEST_MATCH_VERSION_BY_ID_AND_VERSION = "/{id}/{version}/best-match-version";
  public static final String DOCUMENT_BEST_MATCH = "/best-match";
  public static final String VERSIONS_BY_ID = "/{id}/versions";
  public static final String PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION = "/{id}/install";

}
