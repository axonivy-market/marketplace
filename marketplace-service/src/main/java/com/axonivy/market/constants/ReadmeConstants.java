package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadmeConstants {
  public static final String README_FILE_NAME = "README";
  public static final Pattern DEMO_PATTERN = Pattern.compile("(?m)^\\s*##\\sDemo\\s*$",
      Pattern.UNICODE_CHARACTER_CLASS);
  public static final Pattern SETUP_PATTERN = Pattern.compile("(?m)^\\s*##\\sSetup\\s*$",
      Pattern.UNICODE_CHARACTER_CLASS);

}
