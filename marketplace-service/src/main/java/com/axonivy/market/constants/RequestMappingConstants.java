package com.axonivy.market.constants;

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
  public static final String GIT_HUB_LOGIN = "/github/login";
  public static final String AUTH = "/auth";
  public static final String BY_ID = "/{id}";
  public static final String BY_FILE_NAME = "/preview/{imageName}";
  public static final String BY_ID_AND_VERSION = "/{id}/{version}";
  public static final String BEST_MATCH_BY_ID_AND_VERSION = "/{id}/{version}/bestmatch";
  public static final String DOCUMENT_BEST_MATCH = "/best-match/{id}/{artifactId}/{version}/{*path}";
  public static final String VERSIONS_BY_ID = "/{id}/versions";
  public static final String PRODUCT_BY_ID = "/product/{id}";
  public static final String PRODUCT_RATING_BY_ID = "/product/{id}/rating";
  public static final String INSTALLATION_COUNT_BY_ID = "/installation-count/{id}";
  public static final String VERSION_DOWNLOAD_BY_ID = "/{id}/{artifactId}/{version}";
  public static final String PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION = "/{id}/{version}/json";
  public static final String VERSIONS_IN_DESIGNER = "/{id}/designerversions";
  public static final String DESIGNER_INSTALLATION_BY_ID = "/installation/{id}/designer";
  public static final String CUSTOM_SORT = "custom-sort";
  public static final String LATEST_ARTIFACT_DOWNLOAD_URL_BY_ID = "/{id}/artifact";
  public static final String ARTIFACTS_AS_ZIP = "/{id}/{artifactId}/{version}/zip-file";
  public static final String SYNC_ZIP_ARTIFACTS = "zip-sync";
  public static final String EXTERNAL_DOCUMENT = API + "/externaldocument";
  public static final String PRODUCT_MARKETPLACE_DATA = API + "/product-marketplace-data";
  public static final String SECURITY_MONITOR = API + "/security-monitor";
  public static final String RELEASE_PREVIEW = API + "/release-preview";
  public static final String PRODUCT_PUBLIC_RELEASES = "/{id}/releases";
  public static final String PRODUCT_PUBLIC_RELEASE_BY_RELEASE_ID = "/{product-id}/releases/{release-id}";
  public static final String SYNC_RELEASE_NOTES_FOR_PRODUCTS = "/sync-release-notes";
  public static final String PRODUCT_ID = "product-id";
  public static final String RELEASE_ID = "release-id";
  public static final String FEEDBACK_APPROVAL = "/approval";
  public static final String MONITOR_DASHBOARD = API + "/monitor-dashboard";
  public static final String REPOS = "repos";
  public static final String REPOS_REPORT = "/{repo}/{workflow}";
  public static final String REPO = "repo";
  public static final String WORKFLOW = "workflow";
  public static final String FOCUSED = "focus";
  public static final String ERROR_PAGE_404 = "/error-page/404";
}
