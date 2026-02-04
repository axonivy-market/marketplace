package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailConstants {
  public static final String GITHUB_MARKET_ORG_URL = "https://github.com/axonivy-market";
  public static final String UL_START = "<ul style=\"list-style: none; padding-left: 0;\">";
  public static final String UL_END = "</ul>";
  public static final String LI_FORMAT = "<li>⛔ %s</li>";
  public static final String REPO_NAME_HEADER_FORMAT = """
      <p>%d. <a href="%s"><strong>%s</strong></a></p>
      """;
}
