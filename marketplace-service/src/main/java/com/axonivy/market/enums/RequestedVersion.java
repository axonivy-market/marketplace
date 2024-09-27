package com.axonivy.market.enums;

public enum RequestedVersion {
  LATEST_DEV, MINOR_DEV_VERSION, LATEST, ORDINARY;

  public static RequestedVersion findByText(String requestedVersion) {
    switch (requestedVersion) {
      case "dev":
      case "nightly":
      case "sprint":
        return LATEST_DEV;
      case "latest":
        return LATEST;
      default:
        if (requestedVersion.contains("-dev")) {
          return MINOR_DEV_VERSION;
        }
        return ORDINARY;
    }
  }
}
