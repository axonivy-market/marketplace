package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Core common constants defining shared values used across the Core module for general application functionality.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreCommonConstants {
  public static final String SLASH = "/";
  public static final String DOT_SEPARATOR = ".";
  public static final String HYPHEN = "-";
  public static final String COMMA = ",";
  public static final String SINGLE_QUOTE = "'";
  public static final String DIGIT_REGEX = "([0-9]+.*)";
  public static final String DEFAULT_DATE_TIME = "1900-01-01 00:00:00";
  public static final String LIKE_PATTERN = "%%%s%%";
  public static final int ONE = 1;
  public static final int TWO = 2;
  public static final int THREE = 3;
}
