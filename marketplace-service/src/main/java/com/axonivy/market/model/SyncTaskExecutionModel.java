package com.axonivy.market.model;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncTaskExecutionModel {
  private String key;
  private SyncTaskStatus status;
  private Date triggeredAt;
  private Date completedAt;
  private String message;

  public static SyncTaskExecutionModel from(SyncTaskExecution execution) {
    return SyncTaskExecutionModel.builder()
        .key(execution.getType().getKey())
        .status(execution.getStatus())
        .triggeredAt(execution.getTriggeredAt())
        .completedAt(execution.getCompletedAt())
        .message(execution.getMessage())
        .build();
  }
}
