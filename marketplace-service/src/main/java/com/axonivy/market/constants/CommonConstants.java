package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstants {
  public static final String REQUESTED_BY = "X-Requested-By";
  public static final String LOGO_FILE = "logo.png";
  public static final String SLASH = "/";
  public static final String DOT_SEPARATOR = ".";
  public static final String PLUS = "+";
  public static final String DASH_SEPARATOR = "-";
  public static final String SPACE_SEPARATOR = " ";
  public static final String BEARER = "Bearer";
  public static final String DIGIT_REGEX = "([0-9]+.*)";

  public static final String IMAGE_ID_FROM_README = "imageId-";
}
