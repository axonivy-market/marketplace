package com.axonivy.market.aop.aspect;

import com.axonivy.market.aop.annotation.TrackSyncTaskExecution;
import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.MarketException;
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
  private final SyncTaskExecutionService syncTaskExecutionService;

  @Around("@annotation(track)")
  public Object aroundSyncTask(ProceedingJoinPoint pjp, TrackSyncTaskExecution track) throws Throwable {
    SyncTaskType jobType = track.value();
    SyncTaskExecution execution = null;
    try {
      execution = syncTaskExecutionService.start(jobType);
      syncTaskExecutionService.markStatusRunning(execution, SyncTaskConstants.RUNNING_MESSAGE);
      Object result = pjp.proceed();
      syncTaskExecutionService.markStatusSuccess(execution, SyncTaskConstants.SYNC_SUCCESSFULLY_MESSAGE);
      return result;
    } catch (MarketException marketException) {
      log.error(marketException.getMessage());
      throw marketException;
    } catch (Throwable t) {
      if (execution != null) {
        syncTaskExecutionService.markStatusFailure(execution, t.getMessage());
      }
      log.error("Sync task {} failed", jobType, t);
      throw t;
    }
  }
}
