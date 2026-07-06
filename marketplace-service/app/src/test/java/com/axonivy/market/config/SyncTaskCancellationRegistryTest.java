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
  void defaultNotCancelledForAllTypes() {
    for (SyncTaskType type : SyncTaskType.values()) {
      assertFalse(registry.isCancelled(type), "Expected not cancelled by default for " + type);
    }
  }

  @Test
  void cancelSetsFlag() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    assertFalse(registry.isCancelled(type));
    registry.cancel(type);
    assertTrue(registry.isCancelled(type));
  }

  @Test
  void resetClearsFlag() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    registry.cancel(type);
    assertTrue(registry.isCancelled(type));
    registry.reset(type);
    assertFalse(registry.isCancelled(type));
  }

  @Test
  void isCancelledReturnsTrueWhenThreadInterrupted() {
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
  void cancelWithNullThrowsNpe() {
    assertThrows(NullPointerException.class, () -> registry.cancel(null));
  }

  @Test
  void isCancelledWithNullThrowsNpeWhenThreadNotInterrupted() {
    if (Thread.currentThread().isInterrupted()) {
      Thread.interrupted();
    }
    assertThrows(NullPointerException.class, () -> registry.isCancelled(null));
  }
}

