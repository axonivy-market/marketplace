package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstants {
  public static final String REQUESTED_BY = "X-Requested-By";
  public static final String REFERER = "Referer";
  public static final String USER_AGENT = "user-agent";
  public static final String PLUS = "+";
  public static final String SPACE_SEPARATOR = " ";
  public static final String BEARER = "Bearer";
  public static final String IMAGE_ID_PREFIX = "imageId-";
  public static final String IMAGE_EXTENSION = "(.*?).(jpeg|jpg|png|gif)";
  public static final String ID_WITH_NUMBER_PATTERN = "%s-%s";
  public static final String COMPATIBILITY_RANGE_FORMAT = "%s - %s";
  public static final String TEST_REPORT_FILE = "export-test-json-file";
  public static final String NEW_LINE = "\\n";
  public static final int PAGE_SIZE_10 = 10;
  public static final int PAGE_SIZE_20 = 20;
  public static final int ZERO = 0;
  public static final String ZIP_EXTENSION = ".zip";
  public static final String GZ_EXTENSION = ".gz";
  public static final String INDEX_HTML = "index.html";
  public static final String SAFE_PATH_REGEX = "^[a-zA-Z0-9._-]+$";
  public static final String IVY_HEADER = "ivy";
}
