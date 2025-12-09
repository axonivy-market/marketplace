package com.axonivy.market.logging;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobType;
import com.axonivy.market.service.SyncJobExecutionService;
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
public class TrackSyncJobExecutionAspect {

  private final SyncJobExecutionService syncJobExecutionService;

  @Around("@annotation(track)")
  public Object aroundSyncJob(ProceedingJoinPoint pjp, TrackSyncJobExecution track) throws Throwable {
    SyncJobType jobType = track.value();
    SyncJobExecution execution = null;
    try {
      execution = syncJobExecutionService.start(jobType);
      Object result = pjp.proceed();
      syncJobExecutionService.markSuccess(execution, "Sync successfully!");
      return result;
    } catch (Throwable t) {
      if (execution != null) {
        syncJobExecutionService.markFailure(execution, t.getMessage());
      }
      log.error("Sync job {} failed", jobType, t);
      throw t;
    }
  }
}
