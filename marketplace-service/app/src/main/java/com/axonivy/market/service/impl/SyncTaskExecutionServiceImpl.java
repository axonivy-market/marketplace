package com.axonivy.market.service.impl;

import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.SyncTaskInProgressException;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import com.axonivy.market.service.SyncTaskExecutionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
@Builder
public class SyncTaskExecutionServiceImpl implements SyncTaskExecutionService {
  private static final int MESSAGE_MAX_LENGTH = 1024;

  private final SyncTaskExecutionRepository syncTaskExecutionRepo;

  @Transactional
  @Override
  public SyncTaskExecution start(SyncTaskType jobType) {
    Optional<SyncTaskExecution> execution = syncTaskExecutionRepo.findByType(jobType);
    if (execution.isPresent()) {
      SyncTaskExecution existingExecution = execution.get();
      if (isActiveStatus(existingExecution.getStatus())) {
        String syncTaskInProgressMessage = SyncTaskConstants.SYNC_TASK_IN_PROGRESS_MESSAGE_PATTERN.formatted(jobType);
        throw new SyncTaskInProgressException(syncTaskInProgressMessage);
      }

      existingExecution.setStatus(SyncTaskStatus.STARTED);
      existingExecution.setMessage(SyncTaskConstants.STARTED_MESSAGE);
      return syncTaskExecutionRepo.save(existingExecution);
    }

    return createExecution(jobType);
  }

  @Transactional
  @Override
  public void markStatusRunning(SyncTaskExecution execution, String message) {
    updateSyncTask(execution, SyncTaskStatus.RUNNING, message);
  }

  @Transactional
  @Override
  public void markStatusSuccess(SyncTaskExecution execution, String message) {
    updateSyncTask(execution, SyncTaskStatus.SUCCESS, message);
  }

  @Transactional
  @Override
  public void markStatusFailure(SyncTaskExecution execution, String message) {
    updateSyncTask(execution, SyncTaskStatus.FAILED, message);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SyncTaskExecutionModel> getAllSyncTaskExecutions() {
    return Arrays.stream(SyncTaskType.values())
        .map(syncTaskExecutionRepo::findByType)
        .flatMap(Optional::stream)
        .map(SyncTaskExecutionModel::from)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public SyncTaskExecutionModel getSyncTaskExecutionByKey(String key) {
    return SyncTaskType.fromKey(key)
        .flatMap(syncTaskExecutionRepo::findByType)
        .map(SyncTaskExecutionModel::from)
        .orElse(null);
  }

  private SyncTaskExecution createExecution(SyncTaskType type) {
    SyncTaskExecution execution = SyncTaskExecution.builder()
        .status(SyncTaskStatus.STARTED)
        .message(SyncTaskConstants.STARTED_MESSAGE)
        .type(type)
        .build();

    try {
      return syncTaskExecutionRepo.saveAndFlush(execution);
    } catch (DataIntegrityViolationException ex) {
      return syncTaskExecutionRepo.findByType(type)
          .map((SyncTaskExecution existingExecution) -> {
            if (isActiveStatus(existingExecution.getStatus())) {
              String syncTaskInProgressMessage = SyncTaskConstants.SYNC_TASK_IN_PROGRESS_MESSAGE_PATTERN.formatted(type);
              throw new SyncTaskInProgressException(syncTaskInProgressMessage);
            }

            existingExecution.setStatus(SyncTaskStatus.STARTED);
            existingExecution.setMessage(SyncTaskConstants.STARTED_MESSAGE);
            return syncTaskExecutionRepo.save(existingExecution);
          })
          .orElseThrow(() -> ex);
    }
  }

  private void updateSyncTask(SyncTaskExecution execution, SyncTaskStatus status, String message) {
    Objects.requireNonNull(execution, SyncTaskConstants.NON_NULL_SYNC_TASK_MESSAGE);

    if (status == SyncTaskStatus.RUNNING) {
      execution.setLastRunDate(execution.getCompletedDate());
      execution.setCompletedDate(null);
    }

    if (status == SyncTaskStatus.SUCCESS || status == SyncTaskStatus.FAILED) {
      execution.setCompletedDate(LocalDateTime.now());
    }
    execution.setStatus(status);
    execution.setMessage(StringUtils.abbreviate(message, MESSAGE_MAX_LENGTH));
    syncTaskExecutionRepo.save(execution);
  }

  private boolean isActiveStatus(SyncTaskStatus status) {
    return status == SyncTaskStatus.STARTED || status == SyncTaskStatus.RUNNING;
  }
}