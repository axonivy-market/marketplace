package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Sync task constants defining status messages and validation messages for synchronization task execution and
 * monitoring.
 * </p>
 *
 * @author vhhoang
 * @since 15/04/2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SyncTaskConstants {
  public static final String SYNC_TASK_IN_PROGRESS_MESSAGE_PATTERN = "Task %s is already in progress!";
  public static final String STARTED_MESSAGE = "Sync task has started!";
  public static final String RUNNING_MESSAGE = "Sync task is running!";
  public static final String NON_NULL_SYNC_TASK_MESSAGE = "SyncTaskExecution must not be null";
  public static final String SYNC_SUCCESSFULLY_MESSAGE = "Sync successfully!";
  public static final String DEFAULT_SCHEDULE_CRON = "0 30 * * * *";
}
