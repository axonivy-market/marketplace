package com.axonivy.market.config;

import com.axonivy.market.enums.SyncTaskType;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SyncTaskCancellationRegistry {

  private final ConcurrentMap<SyncTaskType, AtomicBoolean> cancellations = new ConcurrentHashMap<>();

  public SyncTaskCancellationRegistry() {
    for (SyncTaskType type : SyncTaskType.values()) {
      cancellations.put(type, new AtomicBoolean(false));
    }
  }

  public void cancel(SyncTaskType type) {
    cancellations.get(type).set(true);
  }

  public void reset(SyncTaskType type) {
    cancellations.get(type).set(false);
  }

  public boolean isCancelled(SyncTaskType type) {
    return Thread.currentThread().isInterrupted()
        || cancellations.getOrDefault(type, new AtomicBoolean(false)).get();
  }
}