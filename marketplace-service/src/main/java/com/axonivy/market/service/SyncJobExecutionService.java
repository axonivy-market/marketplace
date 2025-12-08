package com.axonivy.market.service;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobType;
import com.axonivy.market.model.SyncJobExecutionModel;

import java.util.List;
import java.util.Optional;

public interface SyncJobExecutionService {
  SyncJobExecution start(SyncJobType jobType);
  void markSuccess(SyncJobExecution execution, String message);
  void markFailure(SyncJobExecution execution, String message);
  List<SyncJobExecutionModel> findLatestExecutions();
  SyncJobExecutionModel getLatestExecutionModelByJobKey(String jobKey);
}