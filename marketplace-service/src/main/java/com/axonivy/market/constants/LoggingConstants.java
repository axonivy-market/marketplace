package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingConstants {

  public static final String ENTRY_FORMAT = "    <%s>%s</%s>%n";
  public static final String ENTRY_START = "  <LogEntry>\n";
  public static final String ENTRY_END = "  </LogEntry>\n";
  public static final String DATE_FORMAT = "yyyy-MM-dd";
  public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String LOG_START = "<Logs>\n";
  public static final String LOG_END = "</Logs>";
  public static final String METHOD = "method";
  public static final String ARGUMENTS = "arguments";
  public static final String TIMESTAMP = "timestamp";
  public static final String NO_ARGUMENTS = "No arguments";
  public static final String MARKET_WEBSITE = "marketplace-website";

}
