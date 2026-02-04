package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SyncTaskConstants {
  public static final String TASK_ALREADY_RUNNING_MESSAGE_PATTERN = "Task %s is already running!";
  public static final String STARTED_MESSAGE = "Sync task has started!";
  public static final String RUNNING_MESSAGE = "Sync task is running!";
  public static final String NON_NULL_SYNC_TASK_MESSAGE = "SyncTaskExecution must not be null";
  public static final String SYNC_SUCCESSFULLY_MESSAGE= "Sync successfully!";
}
