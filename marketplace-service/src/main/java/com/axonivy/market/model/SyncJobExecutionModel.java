package com.axonivy.market.model;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobStatus;
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
public class SyncJobExecutionModel {
  String jobKey;
  SyncJobStatus status;
  Date triggeredAt;
  Date completedAt;
  String message;

  public static SyncJobExecutionModel from(SyncJobExecution execution) {
    return SyncJobExecutionModel.builder()
        .jobKey(execution.getJobType().getJobKey())
        .status(execution.getStatus())
        .triggeredAt(execution.getTriggeredAt())
        .completedAt(execution.getCompletedAt())
        .message(execution.getMessage())
        .build();
  }
}
