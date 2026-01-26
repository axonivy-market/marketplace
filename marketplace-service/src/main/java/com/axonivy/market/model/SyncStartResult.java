package com.axonivy.market.model;

import com.axonivy.market.entity.SyncTaskExecution;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SyncStartResult {
  private SyncTaskExecution syncTaskExecution;
  private boolean alreadyRunning;
}
