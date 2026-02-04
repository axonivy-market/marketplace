package com.axonivy.market.aspect;

import com.axonivy.market.aop.annotation.TrackSyncTaskExecution;
import com.axonivy.market.aop.aspect.TrackSyncTaskExecutionAspect;
import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.service.SyncTaskExecutionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TrackSyncTaskExecutionAspectTest {

  private SyncTaskExecutionService syncTaskExecutionService;
  private TrackSyncTaskExecutionAspect aspect;
  private ProceedingJoinPoint pjp;
  private TrackSyncTaskExecution track;

  @BeforeEach
  void setUp() {
    syncTaskExecutionService = mock(SyncTaskExecutionService.class);
    aspect = new TrackSyncTaskExecutionAspect(syncTaskExecutionService);
    pjp = mock(ProceedingJoinPoint.class);
    track = mock(TrackSyncTaskExecution.class);
  }

  @Test
  void testAroundSyncTaskSuccess() throws Throwable {
    SyncTaskExecution execution = new SyncTaskExecution();
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(execution);
    when(pjp.proceed()).thenReturn("result");

    Object result = aspect.aroundSyncTask(pjp, track);

    assertEquals("result", result, "Result should match the value returned by pjp.proceed()");
    verify(syncTaskExecutionService).markStatusSuccess(execution, SyncTaskConstants.SYNC_SUCCESSFULLY_MESSAGE);
    verify(syncTaskExecutionService, never()).markStatusFailure(any(), any());
  }

  @Test
  void testAroundSyncTaskFailure() throws Throwable {
    SyncTaskExecution execution = new SyncTaskExecution();
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(execution);
    String fail = "fail";
    when(pjp.proceed()).thenThrow(new RuntimeException(fail));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> aspect.aroundSyncTask(pjp, track),
        "Should throw RuntimeException when pjp.proceed() fails");
    assertEquals(fail, thrown.getMessage(), "Exception message should match the thrown message");
    verify(syncTaskExecutionService).markStatusFailure(execution, fail);
    verify(syncTaskExecutionService, never()).markStatusSuccess(any(), any());
  }

  @Test
  void testAroundSyncTaskAlreadyRunning() {
    SyncTaskExecution runningSyncTaskExecution = new SyncTaskExecution();
    runningSyncTaskExecution.setStatus(SyncTaskStatus.RUNNING);
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    String errorMessage = SyncTaskConstants.TASK_ALREADY_RUNNING_MESSAGE_PATTERN.formatted(track.value());
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS))
        .thenThrow(new MarketException(ErrorCode.TASK_ALREADY_RUNNING.getCode(), errorMessage));

    MarketException marketException = assertThrows(MarketException.class,
        () -> aspect.aroundSyncTask(pjp, track), "Should throw MarketException when execution status is " +
            "RUNNING");
    assertEquals(errorMessage, marketException.getMessage(),
        "Exception message should match the thrown message");

    verify(syncTaskExecutionService, never()).markStatusRunning(any(), any());
    verify(syncTaskExecutionService, never()).markStatusSuccess(any(), any());
    verify(syncTaskExecutionService, never()).markStatusFailure(any(), any());
  }
}