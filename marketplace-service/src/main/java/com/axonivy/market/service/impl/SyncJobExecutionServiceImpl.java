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
  public SyncJobExecution start(SyncJobType jobType) {
    List<SyncJobExecution> executions = syncJobExecutionRepo.findByJobType(jobType);
    SyncJobExecution execution = executions.isEmpty()
        ? SyncJobExecution.builder().jobType(jobType).build()
        : executions.get(0);

    execution.setStatus(SyncJobStatus.RUNNING);
    execution.setTriggeredAt(new Date());
    execution.setCompletedAt(null);
    execution.setMessage(null);

    return syncJobExecutionRepo.save(execution);
  }

  @Transactional
  public void markSuccess(SyncJobExecution execution, String message) {
    update(execution, SyncJobStatus.SUCCESS, message);
  }

  @Transactional
  public void markFailure(SyncJobExecution execution, String message) {
    update(execution, SyncJobStatus.FAILED, message);
  }

  @Transactional(readOnly = true)
  public List<SyncJobExecutionModel> findLatestExecutions() {
    return Arrays.stream(SyncJobType.values())
        .map(this::findLatestExecution)
        .flatMap(Optional::stream)
        .toList();
  }

  @Transactional(readOnly = true)
  public SyncJobExecutionModel getLatestExecutionModelByJobKey(String jobKey) {
    return SyncJobType.fromJobKey(jobKey)
        .flatMap(this::findLatestExecution)
        .orElse(null);
  }

  @Transactional(readOnly = true)
  private Optional<SyncJobExecutionModel> findLatestExecution(SyncJobType jobType) {
    List<SyncJobExecution> executions = syncJobExecutionRepo.findByJobType(jobType);
    return executions.isEmpty()
        ? Optional.empty()
        : Optional.of(SyncJobExecutionModel.from(executions.get(0)));
  }

  private void update(SyncJobExecution execution, SyncJobStatus status, String message) {
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