package com.axonivy.market.service;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.model.SyncTaskExecutionModel;

import java.util.List;

public interface SyncTaskExecutionService {
  
  /**
   * <p>
   * Initiates a new synchronization task of the specified type and creates a tracking record.
   * Returns the SyncTaskExecution entity with initial status "STARTED" and assigned task ID for
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
   * @param syncTaskType
   *              type {@link SyncTaskType} - the type of sync task to start (PRODUCTS, DOCUMENTS, DEPENDENCIES, etc.)
   * @param message
   *              type {@link String} - progress message or current operation description
   * @author vhhoang
   */
  void markStatusRunning(SyncTaskType syncTaskType, String message);

  /**
   * <p>
   * Updates the synchronization task status to SUCCESS and records completion details. Called when
   * task execution completes successfully to mark task as finished with optional summary message.
   * </p>
   *
   * @param syncTaskType
   *              type {@link SyncTaskType} - the type of sync task to start (PRODUCTS, DOCUMENTS, DEPENDENCIES, etc.)
   * @param message
   *              type {@link String} - completion message or summary of what was synchronized
   * @author nntthuy
   */
  void markStatusSuccess(SyncTaskType syncTaskType, String message);

  /**
   * <p>
   * Updates the synchronization task status to FAILURE and records error details. Called when
   * task execution encounters an error to mark task as failed with error message for troubleshooting.
   * </p>
   *
   * @param syncTaskType
   *              type {@link SyncTaskType} - the type of sync task to start (PRODUCTS, DOCUMENTS, DEPENDENCIES, etc.)
   * @param message
   *              type {@link String} - error message describing why the sync task failed
   * @author nntthuy
   */
  void markStatusFailure(SyncTaskType syncTaskType, String message);

  /**
   * <p>
   * Updates the synchronization task status to CANCELLED and records cancellation details. Called when
   * task execution is cancelled to mark task as cancelled with optional message for monitoring.
   * </p>
   *
   * @param syncTaskType
   *              type {@link SyncTaskType} - the type of sync task to start (PRODUCTS, DOCUMENTS, DEPENDENCIES, etc.)
   * @param message
   *              type {@link String} - cancellation message or reason for cancelling the sync task
   * @author pvquan
   */
  void markStatusCancelled(SyncTaskType syncTaskType, String message);

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

  /**
   * <p>
   * Cancels a running synchronization task identified by its unique job key. If the task is currently
   * executing, it will be stopped and marked as cancelled. Returns true if cancellation was successful,
   * false if the task was not found or could not be cancelled.
   * </p>
   *
   * @param  jobKey
   *              type {@link String} - the unique job key identifying the running sync task
   * @return {@link boolean} - true if the task was successfully cancelled; false if not found or already completed
   * @author pvquan
   */
  boolean cancel(String jobKey);
}