package com.axonivy.market.enums;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SyncTaskTypeTest {

  @Test
  void testFromKey_validKeys() {
    assertEquals(Optional.of(SyncTaskType.SYNC_PRODUCTS), SyncTaskType.fromKey("syncProducts"));
    assertEquals(Optional.of(SyncTaskType.SYNC_ONE_PRODUCT), SyncTaskType.fromKey("syncOneProduct"));
    assertEquals(Optional.of(SyncTaskType.SYNC_RELEASE_NOTES), SyncTaskType.fromKey("syncLatestReleasesForProducts"));
    assertEquals(Optional.of(SyncTaskType.SYNC_GITHUB_MONITOR), SyncTaskType.fromKey("syncGithubMonitor"));
  }

  @Test
  void testFromKey_invalidKey() {
    assertEquals(Optional.empty(), SyncTaskType.fromKey("invalidKey"));
    assertEquals(Optional.empty(), SyncTaskType.fromKey(null));
  }

  @Test
  void testGetKey() {
    assertEquals("syncProducts", SyncTaskType.SYNC_PRODUCTS.getKey());
    assertEquals("syncOneProduct", SyncTaskType.SYNC_ONE_PRODUCT.getKey());
    assertEquals("syncLatestReleasesForProducts", SyncTaskType.SYNC_RELEASE_NOTES.getKey());
    assertEquals("syncGithubMonitor", SyncTaskType.SYNC_GITHUB_MONITOR.getKey());
  }
}