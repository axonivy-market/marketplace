package com.axonivy.market.service.impl;

import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import com.axonivy.market.service.SyncTaskExecutionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    SyncTaskExecution execution = findOrCreate(jobType);
    if (SyncTaskStatus.RUNNING == execution.getStatus()) {
      String taskAlreadyRunningMessage = SyncTaskConstants.TASK_ALREADY_RUNNING_MESSAGE_PATTERN.formatted(jobType);
      throw new MarketException(ErrorCode.TASK_ALREADY_RUNNING.getCode(),
          taskAlreadyRunningMessage);
    }
    execution.setStatus(SyncTaskStatus.STARTED);
    execution.setTriggeredAt(LocalDate.now());
    execution.setCompletedAt(null);
    execution.setMessage(SyncTaskConstants.STARTED_MESSAGE);

    return syncTaskExecutionRepo.save(execution);
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

  private SyncTaskExecution findOrCreate(SyncTaskType type) {
    return syncTaskExecutionRepo.findByType(type)
        .orElseGet(() -> SyncTaskExecution.builder()
            .type(type)
            .build()
        );
  }

  private void updateSyncTask(SyncTaskExecution execution, SyncTaskStatus status, String message) {
    Objects.requireNonNull(execution, SyncTaskConstants.NON_NULL_SYNC_TASK_MESSAGE);
    execution.setStatus(status);
    execution.setCompletedAt(LocalDate.now());
    execution.setMessage(StringUtils.abbreviate(message, MESSAGE_MAX_LENGTH));
    syncTaskExecutionRepo.save(execution);
  }
}