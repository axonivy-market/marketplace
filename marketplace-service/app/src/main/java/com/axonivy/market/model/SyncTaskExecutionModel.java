package com.axonivy.market.model;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncTaskExecutionModel {
  private String key;
  private SyncTaskStatus status;
  private LocalDateTime lastRunDate;
  private LocalDateTime completedDate;
  private String message;
  private Integer version;

  public static SyncTaskExecutionModel from(SyncTaskExecution execution) {
    return SyncTaskExecutionModel.builder()
        .key(execution.getType().getKey())
        .status(execution.getStatus())
        .lastRunDate(execution.getLastRunDate())
        .completedDate(execution.getCompletedDate())
        .message(execution.getMessage())
        .version(execution.getVersion())
        .build();
  }
}
