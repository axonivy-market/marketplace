package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexConstants {

  public static final String SAFE_STRING_REGEX = "^[\\p{L}\\p{N}._\\-]+$";

  public static final Pattern SAFE_STRING_PATTERN = Pattern.compile(SAFE_STRING_REGEX);

}
