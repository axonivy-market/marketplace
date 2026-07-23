package com.axonivy.market.config;

import com.axonivy.market.enums.SyncTaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyncTaskCancellationRegistryTest {

  private SyncTaskCancellationRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SyncTaskCancellationRegistry();
  }

  @Test
  void testDefaultNotCancelledForAllTypes() {
    for (SyncTaskType type : SyncTaskType.values()) {
      assertFalse(registry.isCancelled(type), "Expected not cancelled by default for " + type);
    }
  }

  @Test
  void testCancelSetsFlag() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    assertFalse(registry.isCancelled(type), "Expected not cancelled before cancel for " + type);
    registry.cancel(type);
    assertTrue(registry.isCancelled(type), "Expected cancelled after cancel for " + type);
  }

  @Test
  void testResetClearsFlag() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    registry.cancel(type);
    assertTrue(registry.isCancelled(type), "Flag should be set after cancel for " + type);
    registry.reset(type);
    assertFalse(registry.isCancelled(type), "Flag should be cleared after reset for " + type);
  }

  @Test
  void testIsCancelledReturnsTrueWhenThreadInterrupted() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    boolean wasInterrupted = Thread.currentThread().isInterrupted();
    try {
      Thread.currentThread().interrupt();
      assertTrue(Thread.currentThread().isInterrupted(), "Thread should be interrupted for this test");
      assertTrue(registry.isCancelled(type), "isCancelled should return true when thread is interrupted");
    } finally {
      if (!wasInterrupted) {
        Thread.interrupted();
      }
    }
  }

  @Test
  void testCancelWithNullThrowsNpe() {
    assertThrows(NullPointerException.class, () -> registry.cancel(null), "cancel(null) should throw NullPointerException");
  }

  @Test
  void testIsCancelledWithNullThrowsNpeWhenThreadNotInterrupted() {
    if (Thread.currentThread().isInterrupted()) {
      Thread.interrupted();
    }
    assertThrows(NullPointerException.class, () -> registry.isCancelled(null),
        "isCancelled(null) should throw NullPointerException when thread is not interrupted");
  }
}

