package com.axonivy.market.service;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.model.SyncTaskExecutionModel;

import java.util.List;

public interface SyncTaskExecutionService {
  
  /**
   * <p>
   * Start synchronize task by task type
   * </p>
   *
   * @param  syncTaskType
   *              type {@link SyncTaskType}
   * @return {@link SyncTaskExecution}
   * @author nntthuy
   */
  SyncTaskExecution start(SyncTaskType syncTaskType);

  /**
   * <p>
   * Mark status running for synchronization task
   * </p>
   *
   * @param execution
   *              type {@link SyncTaskExecution}
   * @param message
   *              type {@link String}
   * @return {@link }
   * @author vhhoang
   */
  void markStatusRunning(SyncTaskExecution execution, String message);

  /**
   * <p>
   * Mark status success for synchronization task
   * </p>
   *
   * @param execution
   *              type {@link SyncTaskExecution}
   * @param message
   *              type {@link String}
   * @return {@link }
   * @author nntthuy
   */
  void markStatusSuccess(SyncTaskExecution execution, String message);

  /**
   * <p>
   * Mark status failure for synchronization task
   * </p>
   *
   * @param execution
   *              type {@link SyncTaskExecution}
   * @param message
   *              type {@link String}
   * @return {@link }
   * @author nntthuy
   */
  void markStatusFailure(SyncTaskExecution execution, String message);

  /**
   * <p>
   * Get all synchronization task executions
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link List<SyncTaskExecutionModel>}
   * @author nntthuy
   */
  List<SyncTaskExecutionModel> getAllSyncTaskExecutions();

  /**
   * <p>
   * Get synchronization task executions by key
   * </p>
   *
   * @param  syncTaskKey
   *              type {@link String}
   * @return {@link SyncTaskExecutionModel}
   * @author nntthuy
   */
  SyncTaskExecutionModel getSyncTaskExecutionByKey(String syncTaskKey);
}