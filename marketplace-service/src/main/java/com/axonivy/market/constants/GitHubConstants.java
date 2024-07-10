package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubConstants {
  public static final String AXONIVY_MARKET_ORGANIZATION_NAME = "axonivy-market";
  public static final String AXONIVY_MARKETPLACE_REPO_NAME = "market";
  public static final String AXONIVY_MARKETPLACE_PATH = "market";
  public static final String DEFAULT_BRANCH = "feature/MARP-463-Multilingualism-for-Website";
  public static final String PRODUCT_JSON_FILE_PATH_FORMAT = "%s/product.json";
  public static final String GITHUB_PROVIDER_NAME = "GitHub";
  public static final String GITHUB_GET_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
}
