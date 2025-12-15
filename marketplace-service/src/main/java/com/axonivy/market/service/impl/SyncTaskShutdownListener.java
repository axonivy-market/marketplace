package com.axonivy.market.service.impl;

import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class SyncTaskShutdownListener {

  private final SyncTaskExecutionRepository syncTaskExecutionRepo;
  private final SyncTaskExecutionServiceImpl syncTaskExecutionService;

  @EventListener(ContextClosedEvent.class)
  public void onShutdown() {
    log.info("Application context is shutting down. Marking RUNNING sync jobs as FAILED.");
    Arrays.stream(SyncTaskType.values())
        .map(syncTaskExecutionRepo::findByType)
        .flatMap(Optional::stream)
        .filter(execution -> execution.getStatus() == SyncTaskStatus.RUNNING)
        .forEach(execution -> {
          try {
            syncTaskExecutionService.markStatusFailure(execution, "Application shutdown during execution");
          } catch (Exception e) {
            log.warn("Failed to mark sync job '{}' as FAILED on shutdown", execution.getType(), e);
          }
        });
  }
}