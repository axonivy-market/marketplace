package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadmeConstants {
  public static final String IMAGES = "images";
  public static final String README_FILE = "README.md";
  public static final String README_FILE_NAME = "README";
  public static final String DEMO_PART = "## Demo";
  public static final String SETUP_PART = "## Setup";
  public static final String DEMO_SETUP_TITLE = "(?i)## Demo|## Setup";
  public static final String IMAGE_EXTENSION = "(.*?).(jpeg|jpg|png|gif)";
  public static final String README_IMAGE_FORMAT = "\\(([^)]*?%s[^)]*?)\\)";
  public static final String IMAGE_DOWNLOAD_URL_FORMAT = "(%s)";
  public static final String DESCRIPTION = "description";
  public static final String DEMO = "demo";
  public static final String SETUP = "setup";
}
