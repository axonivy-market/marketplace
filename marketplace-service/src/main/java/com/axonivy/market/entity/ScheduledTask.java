package com.axonivy.market.entity;

import com.axonivy.market.enums.JobStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serial;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.print.attribute.standard.JobState;

import static com.axonivy.market.constants.EntityConstants.SCHEDULED_TASK;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = SCHEDULED_TASK)
public class ScheduledTask extends GenericIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;
  private String name; // ClassName#methodName
  private String cronExpression; // resolved cron expression (if any)
  private Date lastStart; // start time of last execution (success or failure)
  private Date lastEnd; // end time of last execution (success or failure)
  private JobStatus status;
  //  private Instant lastSuccessEnd; // end time of last successful execution
  private Date nextExecution; // next expected execution time (cron only)
  private boolean running; // true while currently executing
//  private boolean lastSuccess; // true if last execution ended without exception
//  private String lastError; // last error message if failed

}