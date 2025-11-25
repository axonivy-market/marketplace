package com.axonivy.market.model;

import com.axonivy.market.enums.SyncJobStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class SyncJobExecutionModel {
  String jobKey;
  SyncJobStatus status;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  Date triggeredAt;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  Date completedAt;
  String message;
  String reference;
}
