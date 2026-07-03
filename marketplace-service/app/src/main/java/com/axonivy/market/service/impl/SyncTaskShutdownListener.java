package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import com.axonivy.market.service.SyncTaskExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class SyncTaskShutdownListener {

  private final SyncTaskExecutionRepository syncTaskExecutionRepo;
  private final SyncTaskExecutionService syncTaskExecutionService;

  @EventListener(ContextClosedEvent.class)
  public void onShutdown() {
    log.info("Application context is shutting down. Marking STARTED and RUNNING sync jobs as FAILED.");
    syncTaskExecutionRepo.findByStatusIn(List.of(SyncTaskStatus.STARTED, SyncTaskStatus.RUNNING))
        .forEach(execution -> {
      try {
        syncTaskExecutionService.markStatusFailure(execution.getType(), "Application shutdown during execution");
      } catch (Exception e) {
        log.warn("Failed to mark sync job '{}' as FAILED on shutdown", execution.getType(), e);
      }
    });
  }
}