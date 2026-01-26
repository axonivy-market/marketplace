package com.axonivy.market.aop.aspect;

import com.axonivy.market.aop.annotation.TrackSyncTaskExecution;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.TaskAlreadyRunningException;
import com.axonivy.market.model.SyncStartResult;
import com.axonivy.market.service.SyncTaskExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class TrackSyncTaskExecutionAspect {

  private static final String TASK_ALREADY_RUNNING_MESSAGE_PATTERN = "Task %s is already running!";
  private final SyncTaskExecutionService syncTaskExecutionService;

  @Around("@annotation(track)")
  public Object aroundSyncTask(ProceedingJoinPoint pjp, TrackSyncTaskExecution track) throws Throwable {
    SyncTaskType jobType = track.value();
    SyncStartResult syncStartResult = null;
    try {
      syncStartResult = syncTaskExecutionService.start(jobType);
      if (syncStartResult.isAlreadyRunning()) {
        String taskAlreadyRunningMessage = TASK_ALREADY_RUNNING_MESSAGE_PATTERN.formatted(jobType);
        throw new TaskAlreadyRunningException(taskAlreadyRunningMessage);
      }
      Object result = pjp.proceed();
      syncTaskExecutionService.markStatusSuccess(syncStartResult.getSyncTaskExecution(), "Sync successfully!");
      return result;
    } catch (TaskAlreadyRunningException taskAlreadyRunningException) {
      log.error(taskAlreadyRunningException.getMessage());
      throw taskAlreadyRunningException;
    } catch (Throwable t) {
      if (syncStartResult != null) {
        syncTaskExecutionService.markStatusFailure(syncStartResult.getSyncTaskExecution(), t.getMessage());
      }
      log.error("Sync task {} failed", jobType, t);
      throw t;
    }
  }
}
