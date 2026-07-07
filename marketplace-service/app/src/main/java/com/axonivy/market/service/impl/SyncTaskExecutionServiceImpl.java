package com.axonivy.market.service.impl;

import com.axonivy.market.config.SyncTaskCancellationRegistry;
import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.SyncTaskInProgressException;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import com.axonivy.market.service.SyncTaskExecutionService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SyncTaskExecutionServiceImpl implements SyncTaskExecutionService {
  private static final int MESSAGE_MAX_LENGTH = 1024;

  private final SyncTaskExecutionRepository syncTaskExecutionRepo;
  private final SyncTaskCancellationRegistry cancellationRegistry;
  @Value("${market.node-number:1}")
  private int nodeNumber;

  @Transactional
  @Override
  public SyncTaskExecution start(SyncTaskType jobType) {
    Optional<SyncTaskExecution> execution = syncTaskExecutionRepo.findByTypeAndNodeNumber(jobType, nodeNumber);
    if (execution.isPresent()) {
      SyncTaskExecution existingExecution = execution.get();
      return restartSyncTaskExecution(jobType, existingExecution);
    }

    return createExecution(jobType);
  }

  @Transactional
  @Override
  public void markStatusRunning(SyncTaskType syncTaskType, String message) {
    updateSyncTask(syncTaskType, SyncTaskStatus.RUNNING, message);
  }

  @Transactional
  @Override
  public void markStatusSuccess(SyncTaskType syncTaskType, String message) {
    updateSyncTask(syncTaskType, SyncTaskStatus.SUCCESS, message);
  }

  @Transactional
  @Override
  public void markStatusFailure(SyncTaskType syncTaskType, String message) {
    updateSyncTask(syncTaskType, SyncTaskStatus.FAILED, message);
  }

  @Transactional
  @Override
  public void markStatusCancelled(SyncTaskType syncTaskType, String message) {
    updateSyncTask(syncTaskType, SyncTaskStatus.CANCELLED, message);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SyncTaskExecutionModel> getAllSyncTaskExecutions() {
    return Arrays.stream(SyncTaskType.values())
        .map(type -> syncTaskExecutionRepo.findByTypeAndNodeNumber(type, nodeNumber))
        .flatMap(Optional::stream)
        .map(SyncTaskExecutionModel::from)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public SyncTaskExecutionModel getSyncTaskExecutionByKey(String key) {
    return SyncTaskType.fromKey(key)
        .flatMap(type -> syncTaskExecutionRepo.findByTypeAndNodeNumber(type, nodeNumber))
        .map(SyncTaskExecutionModel::from)
        .orElse(null);
  }

  @Override
  public boolean cancel(String jobKey) {
    Optional<SyncTaskType> type = SyncTaskType.fromKey(jobKey);
    return type.map(syncTaskType -> syncTaskExecutionRepo.findByTypeAndNodeNumber(syncTaskType, nodeNumber)
        .filter(execution -> execution.getStatus() == SyncTaskStatus.RUNNING
            || execution.getStatus() == SyncTaskStatus.STARTED)
        .map((SyncTaskExecution task) -> {
          cancellationRegistry.cancel(syncTaskType);
          return true;
        })
        .orElse(false)).orElse(false);
  }

  /**
   * Creates the sync task row in STARTED state so other nodes can observe that the task has already been claimed.
   * If another node wins the insert race first, the unique-constraint failure is converted into either an
   * in-progress error or a reuse of the existing non-active row.
   */
  private SyncTaskExecution createExecution(SyncTaskType type) {
    SyncTaskExecution execution = SyncTaskExecution.builder()
        .status(SyncTaskStatus.STARTED)
        .message(SyncTaskConstants.STARTED_MESSAGE)
        .type(type)
        .nodeNumber(nodeNumber)
        .build();

    try {
      return syncTaskExecutionRepo.saveAndFlush(execution);
    } catch (DataIntegrityViolationException ex) {
      return syncTaskExecutionRepo.findByTypeAndNodeNumber(type, nodeNumber)
          .map((SyncTaskExecution existingExecution) -> restartSyncTaskExecution(type, existingExecution))
          .orElseThrow(() -> ex);
    }
  }

  private SyncTaskExecution restartSyncTaskExecution(SyncTaskType jobType, SyncTaskExecution existingExecution) {
    if (isActiveStatus(existingExecution.getStatus())) {
      String syncTaskInProgressMessage = SyncTaskConstants.SYNC_TASK_IN_PROGRESS_MESSAGE_PATTERN.formatted(jobType);
      throw new SyncTaskInProgressException(syncTaskInProgressMessage);
    }

    existingExecution.setStatus(SyncTaskStatus.STARTED);
    existingExecution.setMessage(SyncTaskConstants.STARTED_MESSAGE);

    try {
      return syncTaskExecutionRepo.saveAndFlush(existingExecution);
    } catch (ObjectOptimisticLockingFailureException ex) {
      log.warn("Concurrent restart detected for sync task {}, treating as in-progress", jobType, ex);
      String syncTaskInProgressMessage = SyncTaskConstants.SYNC_TASK_IN_PROGRESS_MESSAGE_PATTERN.formatted(jobType);
      throw new SyncTaskInProgressException(syncTaskInProgressMessage);
    }
  }

  private void updateSyncTask(SyncTaskType syncTaskType, SyncTaskStatus status, String message) {
    Optional<SyncTaskExecution> execution = syncTaskExecutionRepo.findByTypeAndNodeNumber(syncTaskType, nodeNumber);
    if (execution.isEmpty()) {
      return;
    }
    SyncTaskExecution taskExecution = execution.get();
    Objects.requireNonNull(taskExecution, SyncTaskConstants.NON_NULL_SYNC_TASK_MESSAGE);

    if (status == SyncTaskStatus.RUNNING) {
      taskExecution.setLastRunDate(taskExecution.getCompletedDate());
      taskExecution.setCompletedDate(null);
    }

    if (status == SyncTaskStatus.SUCCESS || status == SyncTaskStatus.FAILED || status == SyncTaskStatus.CANCELLED) {
      taskExecution.setCompletedDate(LocalDateTime.now());
    }
    taskExecution.setStatus(status);
    taskExecution.setMessage(StringUtils.abbreviate(message, MESSAGE_MAX_LENGTH));

    try {
      syncTaskExecutionRepo.saveAndFlush(taskExecution);
    } catch (ObjectOptimisticLockingFailureException ex) {
      log.warn("Concurrent update detected for sync task {}, skipping status update to {}", taskExecution.getType(),
          status, ex);
    }
  }

  private boolean isActiveStatus(SyncTaskStatus status) {
    return status == SyncTaskStatus.STARTED || status == SyncTaskStatus.RUNNING;
  }

  /**
   * <p>
   * Initializes the cancellation registry for sync tasks that were in RUNNING state before application restart.
   * This ensures that any tasks that were interrupted due to a restart are marked as FAILED and can be restarted
   * cleanly.
   * </p>
   */
  @PostConstruct
  private void initializeCancellationRegistry() {
    List<SyncTaskExecution> taskExecutions =
        syncTaskExecutionRepo.findByNodeNumberAndStatusIn(nodeNumber, List.of(SyncTaskStatus.RUNNING));
    if (taskExecutions.isEmpty()) {
      return;
    }
    for (SyncTaskExecution execution : taskExecutions) {
      execution.setStatus(SyncTaskStatus.FAILED);
      execution.setMessage("Sync task was interrupted due to application restart.");
      execution.setCompletedDate(LocalDateTime.now());
    }
    syncTaskExecutionRepo.saveAll(taskExecutions);
  }
}