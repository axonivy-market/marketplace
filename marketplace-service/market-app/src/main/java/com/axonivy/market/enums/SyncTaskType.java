package com.axonivy.market.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum SyncTaskType {
  SYNC_PRODUCTS("syncProducts"),
  SYNC_ONE_PRODUCT("syncOneProduct"),
  SYNC_RELEASE_NOTES("syncLatestReleasesForProducts"),
  SYNC_GITHUB_MONITOR("syncGithubMonitor");

  private final String key;

  public static Optional<SyncTaskType> fromKey(String key) {
    return Arrays.stream(values())
        .filter(type -> type.key.equals(key))
        .findFirst();
  }
}
