package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstants {
  public static final String REQUESTED_BY = "X-Requested-By";
  public static final String USER_AGENT = "user-agent";
  public static final String SLASH = "/";
  public static final String DOT_SEPARATOR = ".";
  public static final String PLUS = "+";
  public static final String DASH_SEPARATOR = "-";
  public static final String SPACE_SEPARATOR = " ";
  public static final String COMMA = ",";
  public static final String BEARER = "Bearer";
  public static final String DIGIT_REGEX = "([0-9]+.*)";
  public static final String IMAGE_ID_PREFIX = "imageId-";
  public static final String IMAGE_EXTENSION = "(.*?).(jpeg|jpg|png|gif)";
  public static final String ID_WITH_NUMBER_PATTERN = "%s-%s";
  public static final String ERROR = "error";
  public static final String MESSAGE = "message";
  public static final String COMPATIBILITY_RANGE_FORMAT = "%s - %s";
  public static final String DEFAULT_DATE_TIME = "1900-01-01 00:00:00";
  public static final String LIKE_PATTERN = "%%%s%%";
  public static final String TEST_REPORT_FILE = "export-test-json-file";
  public static final String NEW_LINE = "\\n";
  public static final String PROD_ENV = "production";
  public static final int PAGE_SIZE_10 = 10;
  public static final int PAGE_SIZE_20 = 20;
  public static final int ZERO = 0;
  public static final int ONE = 1;
  public static final int TWO = 2;
  public static final int THREE = 3;
  public static final String ZIP_EXTENSION = ".zip";
  public static final String INDEX_HTML = "index.html";
  public static final String SAFE_PATH_REGEX = "^[a-zA-Z0-9._-]+$";
}
