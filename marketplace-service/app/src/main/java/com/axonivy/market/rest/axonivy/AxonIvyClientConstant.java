package com.axonivy.market.rest.axonivy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AxonIvyClientConstant {
  public static final String DOCUMENT_VERSION_PATH = "/api/docs/Axon-Ivy-Platform/dev/en";
  public static final String HOST_PATH_FORMAT = "%s%s";
  public static final String DEV_VERSION = "dev";
  public static final Pattern VERSION_FROM_URL_PATTERN = Pattern.compile("/doc/([\\d.]+)/");
}

