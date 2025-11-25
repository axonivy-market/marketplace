package com.axonivy.market.model;

import com.axonivy.market.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
//@Relation(collectionRelation = "feedbacks", itemRelation = "feedback")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ScheduledTaskModel {
  @EqualsAndHashCode.Include
  @Schema(description = "Id of scheduled task", example = "667940ecc881b1d0db072f9e")
  private String id;
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
