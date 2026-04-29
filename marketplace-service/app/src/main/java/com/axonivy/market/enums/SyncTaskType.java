package com.axonivy.market.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * <p>
 * Synchronization task type enumeration defining different types of background synchronization operations.
 * </p>
 *
 * @since 15/04/2026
 * @author nntthuy
 */
@Getter
@RequiredArgsConstructor
public enum SyncTaskType {
  SYNC_PRODUCTS("syncProducts"),
  SYNC_ONE_PRODUCT("syncOneProduct"),
  SYNC_RELEASE_NOTES("syncLatestReleasesForProducts"),
  SYNC_GITHUB_MONITOR("syncGithubMonitor"),
  SYNC_GITHUB_SECURITY_MONITOR("syncGithubSecurityMonitor");

  private final String key;

  public static Optional<SyncTaskType> fromKey(String key) {
    return Arrays.stream(values())
        .filter(type -> type.key.equals(key))
        .findFirst();
  }
}
