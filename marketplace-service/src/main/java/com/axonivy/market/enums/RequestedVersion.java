package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

public enum RequestedVersion {
  LATEST_DEV_OF_VERSION, LATEST, ORDINARY, RELEASE;

  public static RequestedVersion findByText(String requestedVersion) {
    switch (requestedVersion) {
      case "dev":
      case "nightly":
      case "sprint":
        return LATEST;
      case "latest":
        return RELEASE;
      default:
        if (StringUtils.isNotBlank(requestedVersion)) {
          if (requestedVersion.contains("-dev")) {
            return LATEST_DEV_OF_VERSION;
          }
        }
        return ORDINARY;
    }
  }
}
