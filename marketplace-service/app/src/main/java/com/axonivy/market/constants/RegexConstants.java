package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * <p>
 * Regex constants defining safe string patterns and compiled patterns for input validation and file name sanitization.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexConstants {

  public static final String SAFE_STRING_REGEX = "^[\\p{L}\\p{N}._\\-]+$";
  public static final Pattern SAFE_STRING_PATTERN = Pattern.compile(SAFE_STRING_REGEX);

  public static final String SAFE_PATH_REGEX = "^[\\p{L}\\p{N}._-]+$";
  public static final Pattern SAFE_PATH_PATTERN = Pattern.compile(SAFE_PATH_REGEX);

  public static final String SAFE_FILE_NAME_REGEX = "[^\\p{L}\\p{N}._-]";
  public static final Pattern SAFE_FILE_NAME_PATTERN = Pattern.compile(SAFE_FILE_NAME_REGEX);

}
