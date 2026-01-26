package com.axonivy.market.service;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.model.SyncStartResult;
import com.axonivy.market.model.SyncTaskExecutionModel;

import java.util.List;

public interface SyncTaskExecutionService {
  SyncStartResult start(SyncTaskType syncTaskType);

  void markStatusSuccess(SyncTaskExecution execution, String message);

  void markStatusFailure(SyncTaskExecution execution, String message);

  List<SyncTaskExecutionModel> getAllSyncTaskExecutions();

  SyncTaskExecutionModel getSyncTaskExecutionByKey(String syncTaskKey);
}