package com.axonivy.market.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum SyncJobType {
  SYNC_PRODUCTS("syncProducts"),
  SYNC_ONE_PRODUCT("syncOneProduct"),
  SYNC_RELEASE_NOTES("syncLatestReleasesForProducts"),
  SYNC_GITHUB_MONITOR("syncGithubMonitor");

  private final String jobKey;

  public static Optional<SyncJobType> fromJobKey(String jobKey) {
    return Arrays.stream(values())
        .filter(type -> type.jobKey.equals(jobKey))
        .findFirst();
  }
}
