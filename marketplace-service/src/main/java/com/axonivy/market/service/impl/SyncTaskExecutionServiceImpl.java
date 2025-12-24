package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
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
import java.util.Date;
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
    execution.setStatus(SyncTaskStatus.RUNNING);
    execution.setTriggeredAt(LocalDate.now());
    execution.setCompletedAt(null);
    execution.setMessage(null);

    return syncTaskExecutionRepo.save(execution);
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
    Objects.requireNonNull(execution, "SyncTaskExecution must not be null");
    execution.setStatus(status);
    execution.setCompletedAt(LocalDate.now());
    execution.setMessage(StringUtils.abbreviate(message, MESSAGE_MAX_LENGTH));
    syncTaskExecutionRepo.save(execution);
  }
}