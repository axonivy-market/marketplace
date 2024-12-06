package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubConstants {
  public static final String AXONIVY_MARKET_ORGANIZATION_NAME = "axonivy-market";
  public static final String AXONIVY_MARKET_TEAM_NAME = "team-octopus";
  public static final String AXONIVY_MARKETPLACE_REPO_NAME = "market";
  public static final String AXONIVY_MARKETPLACE_PATH = "market";
  public static final String GITHUB_PROVIDER_NAME = "GitHub";
  public static final String GITHUB_GET_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
  public static final String README_FILE_LOCALE_REGEX = "_(..)";

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Json {
    public static final String TOKEN = "token";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CODE = "code";
    public static final String SEVERITY = "severity";
    public static final String SECURITY_SEVERITY_LEVEL = "security_severity_level";
    public static final String SEVERITY_ADVISORY = "security_advisory";
    public static final String RULE = "rule";
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Url {
    private static final String BASE_URL = "https://api.github.com";
    public static final String USER = BASE_URL + "/user";
    public static final String REPO_SECURITY_ADVISORIES = BASE_URL + "/repos/%s/%s/security-advisories?state=%s";
    public static final String REPO_DEPENDABOT_ALERTS_OPEN = BASE_URL + "/repos/%s/%s/dependabot/alerts?state=open";
    public static final String REPO_SECRET_SCANNING_ALERTS_OPEN =
        BASE_URL + "/repos/%s/%s/secret-scanning/alerts?state=open";
    public static final String REPO_CODE_SCANNING_ALERTS_OPEN =
        BASE_URL + "/repos/%s/%s/code-scanning/alerts?state=open";
  }
}
