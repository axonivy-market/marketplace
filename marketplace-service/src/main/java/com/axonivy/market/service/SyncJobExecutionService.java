package com.axonivy.market.service;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobType;
import com.axonivy.market.model.SyncJobExecutionModel;

import java.util.List;

public interface SyncJobExecutionService {
  SyncJobExecution start(SyncJobType jobType);

  void markStatusSuccess(SyncJobExecution execution, String message);

  void markStatusFailure(SyncJobExecution execution, String message);

  List<SyncJobExecutionModel> getAllSyncJobExecutions();

  SyncJobExecutionModel getSyncJobExecutionByKey(String jobKey);
}