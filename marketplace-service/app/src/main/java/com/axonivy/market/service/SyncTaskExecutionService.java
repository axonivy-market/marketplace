package com.axonivy.market.service;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.model.SyncTaskExecutionModel;

import java.util.List;

public interface SyncTaskExecutionService {
  
  /**
   * <p>
   * Initiates a new synchronization task of the specified type and creates a tracking record.
   * Returns the SyncTaskExecution entity with initial status "PENDING" and assigned task ID for
   * monitoring task progress and results.
   * </p>
   *
   * @param  syncTaskType
   *              type {@link SyncTaskType} - the type of sync task to start (PRODUCTS, DOCUMENTS, DEPENDENCIES, etc.)
   * @return {@link SyncTaskExecution} - created execution record with task ID, start time, and initial status
   * @author nntthuy
   */
  SyncTaskExecution start(SyncTaskType syncTaskType);

  /**
   * <p>
   * Updates the synchronization task status to RUNNING and logs a progress message. Called when
   * the task begins actual execution to track task progress and provide status updates.
   * </p>
   *
   * @param execution
   *              type {@link SyncTaskExecution} - the execution record to update
   * @param message
   *              type {@link String} - progress message or current operation description
   * @return void - status is updated immediately in the database
   * @author vhhoang
   */
  void markStatusRunning(SyncTaskExecution execution, String message);

  /**
   * <p>
   * Updates the synchronization task status to SUCCESS and records completion details. Called when
   * task execution completes successfully to mark task as finished with optional summary message.
   * </p>
   *
   * @param execution
   *              type {@link SyncTaskExecution} - the execution record to update
   * @param message
   *              type {@link String} - completion message or summary of what was synchronized
   * @return void - status and end time are updated immediately in the database
   * @author nntthuy
   */
  void markStatusSuccess(SyncTaskExecution execution, String message);

  /**
   * <p>
   * Updates the synchronization task status to FAILURE and records error details. Called when
   * task execution encounters an error to mark task as failed with error message for troubleshooting.
   * </p>
   *
   * @param execution
   *              type {@link SyncTaskExecution} - the execution record to update
   * @param message
   *              type {@link String} - error message describing why the sync task failed
   * @return void - status and end time are updated immediately in the database
   * @author nntthuy
   */
  void markStatusFailure(SyncTaskExecution execution, String message);

  /**
   * <p>
   * Retrieves all synchronization task execution records from the system. Returns a complete history
   * of all sync tasks with their status, execution time, and results for monitoring and auditing purposes.
   * </p>
   *
   * @return {@link List<SyncTaskExecutionModel>} - list of all synchronization task execution records
   *         sorted by execution time (newest first); returns empty list if no executions recorded
   * @author nntthuy
   */
  List<SyncTaskExecutionModel> getAllSyncTaskExecutions();

  /**
   * <p>
   * Retrieves a specific synchronization task execution by its unique task key. Returns detailed
   * information about a single task execution including status, start/end time, and execution logs.
   * </p>
   *
   * @param  syncTaskKey
   *              type {@link String} - the unique task key identifying the specific execution
   * @return {@link SyncTaskExecutionModel} - detailed execution record with all status and timing information;
   *         returns null if task key not found
   * @author nntthuy
   */
  SyncTaskExecutionModel getSyncTaskExecutionByKey(String syncTaskKey);
}