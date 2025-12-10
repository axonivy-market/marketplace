package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobStatus;
import com.axonivy.market.enums.SyncJobType;
import com.axonivy.market.model.SyncJobExecutionModel;
import com.axonivy.market.repository.SyncJobExecutionRepository;
import com.axonivy.market.service.SyncJobExecutionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
@Builder
public class SyncJobExecutionServiceImpl implements SyncJobExecutionService {
  private static final int MESSAGE_MAX_LENGTH = 1024;

  private final SyncJobExecutionRepository syncJobExecutionRepo;

  @Transactional
  @Override
  public SyncJobExecution start(SyncJobType jobType) {
    SyncJobExecution execution = findOrCreate(jobType);
    execution.setStatus(SyncJobStatus.RUNNING);
    execution.setTriggeredAt(new Date());
    execution.setCompletedAt(null);
    execution.setMessage(null);

    return syncJobExecutionRepo.save(execution);
  }

  @Transactional
  @Override
  public void markStatusSuccess(SyncJobExecution execution, String message) {
    updateSyncJob(execution, SyncJobStatus.SUCCESS, message);
  }

  @Transactional
  @Override
  public void markStatusFailure(SyncJobExecution execution, String message) {
    updateSyncJob(execution, SyncJobStatus.FAILED, message);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SyncJobExecutionModel> getAllSyncJobExecutions() {
    return Arrays.stream(SyncJobType.values())
        .map(syncJobExecutionRepo::findByJobType)
        .flatMap(Optional::stream)
        .map(SyncJobExecutionModel::from)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public SyncJobExecutionModel getSyncJobExecutionByKey(String jobKey) {
    return SyncJobType.fromJobKey(jobKey)
        .flatMap(syncJobExecutionRepo::findByJobType)
        .map(SyncJobExecutionModel::from)
        .orElse(null);
  }

  private SyncJobExecution findOrCreate(SyncJobType type) {
    return syncJobExecutionRepo.findByJobType(type)
        .orElseGet(() -> SyncJobExecution.builder()
            .jobType(type)
            .build()
        );
  }

  private void updateSyncJob(SyncJobExecution execution, SyncJobStatus status, String message) {
    execution.setStatus(status);
    execution.setCompletedAt(new Date());
    execution.setMessage(trim(message));
    syncJobExecutionRepo.save(execution);
  }

  private String trim(String message) {
    if (StringUtils.isBlank(message)) {
      return null;
    }
    return StringUtils.abbreviate(message, MESSAGE_MAX_LENGTH);
  }
}