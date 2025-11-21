package com.axonivy.market.schedulingtask;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "scheduled_task")
public class ScheduledTaskEntity {
  @Id
  private String id; // ClassName#methodName
  private Instant lastEnd; // end time of last execution (success or failure)
  private Instant lastSuccessEnd; // end time of last successful execution
  private Instant nextExecution; // next expected execution time (cron only)
  private boolean running; // true while currently executing
  private boolean lastSuccess; // true if last execution ended without exception
//  private String lastError; // last error message if failed


  private String cronExpression; // resolved cron expression (if any)
  private Instant lastStart; // start time of last execution (success or failure)
}