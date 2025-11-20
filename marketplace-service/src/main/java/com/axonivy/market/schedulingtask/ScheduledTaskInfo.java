package com.axonivy.market.schedulingtask;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class ScheduledTaskInfo {
  private String id; // ClassName#methodName
  private String cronExpression; // resolved cron expression (if any)
  private Instant lastStart; // start time of last execution (success or failure)
  private Instant lastEnd; // end time of last execution (success or failure)
  private Instant lastSuccessEnd; // end time of last successful execution
  private Instant nextExecution; // next expected execution time (cron only)
  private boolean running; // true while currently executing
  private boolean lastSuccess; // true if last execution ended without exception
  private String lastError; // last error message if failed
}
