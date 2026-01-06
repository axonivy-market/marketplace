package com.axonivy.market.enums;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SyncTaskTypeTest {

  @Test
  void testFromKeyValidKeys() {
    assertEquals(Optional.of(SyncTaskType.SYNC_PRODUCTS), SyncTaskType.fromKey("syncProducts"), "Should return SYNC_PRODUCTS for 'syncProducts' key");
    assertEquals(Optional.of(SyncTaskType.SYNC_ONE_PRODUCT), SyncTaskType.fromKey("syncOneProduct"), "Should return SYNC_ONE_PRODUCT for 'syncOneProduct' key");
    assertEquals(Optional.of(SyncTaskType.SYNC_RELEASE_NOTES), SyncTaskType.fromKey("syncLatestReleasesForProducts"), "Should return SYNC_RELEASE_NOTES for 'syncLatestReleasesForProducts' key");
    assertEquals(Optional.of(SyncTaskType.SYNC_GITHUB_MONITOR), SyncTaskType.fromKey("syncGithubMonitor"), "Should return SYNC_GITHUB_MONITOR for 'syncGithubMonitor' key");
  }

  @Test
  void testFromKeyInvalidKey() {
    assertEquals(Optional.empty(), SyncTaskType.fromKey("invalidKey"), "Should return empty for invalid key");
    assertEquals(Optional.empty(), SyncTaskType.fromKey(null), "Should return empty for null key");
  }

  @Test
  void testGetKey() {
    assertEquals("syncProducts", SyncTaskType.SYNC_PRODUCTS.getKey(), "SYNC_PRODUCTS key should be 'syncProducts'");
    assertEquals("syncOneProduct", SyncTaskType.SYNC_ONE_PRODUCT.getKey(), "SYNC_ONE_PRODUCT key should be 'syncOneProduct'");
    assertEquals("syncLatestReleasesForProducts", SyncTaskType.SYNC_RELEASE_NOTES.getKey(), "SYNC_RELEASE_NOTES key should be 'syncLatestReleasesForProducts'");
    assertEquals("syncGithubMonitor", SyncTaskType.SYNC_GITHUB_MONITOR.getKey(), "SYNC_GITHUB_MONITOR key should be 'syncGithubMonitor'");
  }
}