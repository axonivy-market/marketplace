package com.axonivy.market.service;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobStatus;
import com.axonivy.market.enums.SyncJobType;
import com.axonivy.market.repository.SyncJobExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SyncJobExecutionService {

  private static final int MESSAGE_MAX_LENGTH = 1024;
  private static final int REFERENCE_MAX_LENGTH = 255;

  private final SyncJobExecutionRepository repository;

  @Transactional
  public SyncJobExecution start(SyncJobType jobType, String reference) {
    var execution = SyncJobExecution.builder()
        .jobType(jobType)
        .status(SyncJobStatus.RUNNING)
        .triggeredAt(new Date())
        .reference(trim(reference, REFERENCE_MAX_LENGTH))
        .build();
    return repository.save(execution);
  }

  @Transactional
  public SyncJobExecution markSuccess(SyncJobExecution execution, String message) {
    return update(execution, SyncJobStatus.SUCCESS, message);
  }

  @Transactional
  public SyncJobExecution markFailure(SyncJobExecution execution, String message) {
    return update(execution, SyncJobStatus.FAILED, message);
  }

  @Transactional(readOnly = true)
  public List<SyncJobExecution> findLatestExecutions() {
    return Arrays.stream(SyncJobType.values())
        .map(this::findLatestExecution)
        .flatMap(Optional::stream)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<SyncJobExecution> findLatestExecution(SyncJobType jobType) {
    return repository.findTopByJobTypeOrderByTriggeredAtDesc(jobType);
  }

  private SyncJobExecution update(SyncJobExecution execution, SyncJobStatus status, String message) {
    execution.setStatus(status);
    execution.setCompletedAt(new Date());
    execution.setMessage(trim(message, MESSAGE_MAX_LENGTH));
    return repository.save(execution);
  }

  private String trim(String source, int maxLength) {
    if (StringUtils.isBlank(source)) {
      return null;
    }
    return StringUtils.abbreviate(source, maxLength);
  }
}
