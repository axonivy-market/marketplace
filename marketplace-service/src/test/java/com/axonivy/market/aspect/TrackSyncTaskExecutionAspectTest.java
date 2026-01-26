package com.axonivy.market.aspect;

import com.axonivy.market.aop.annotation.TrackSyncTaskExecution;
import com.axonivy.market.aop.aspect.TrackSyncTaskExecutionAspect;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.TaskAlreadyRunningException;
import com.axonivy.market.model.SyncStartResult;
import com.axonivy.market.service.SyncTaskExecutionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
    SyncStartResult syncStartResult = getSyncStartResult(false);
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(syncStartResult);
    when(pjp.proceed()).thenReturn("result");

    Object result = aspect.aroundSyncTask(pjp, track);

    assertEquals("result", result, "Result should match the value returned by pjp.proceed()");
    verify(syncTaskExecutionService).markStatusSuccess(syncStartResult.getSyncTaskExecution(), "Sync successfully!");
    verify(syncTaskExecutionService, never()).markStatusFailure(any(), any());
  }

  @Test
  void testAroundSyncTaskFailure() throws Throwable {
    SyncStartResult syncStartResult = getSyncStartResult(false);
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(syncStartResult);
    String fail = "fail";
    when(pjp.proceed()).thenThrow(new RuntimeException(fail));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> aspect.aroundSyncTask(pjp, track),
        "Should throw RuntimeException when pjp.proceed() fails");
    assertEquals(fail, thrown.getMessage(), "Exception message should match the thrown message");
    verify(syncTaskExecutionService).markStatusFailure(syncStartResult.getSyncTaskExecution(), fail);
    verify(syncTaskExecutionService, never()).markStatusSuccess(any(), any());
  }

  @Test
  void testAroundSyncTaskAlreadyRunning() throws Throwable {
    String TASK_ALREADY_RUNNING_MESSAGE_PATTERN = "Task %s is already running!";
    SyncStartResult syncStartResult = getSyncStartResult(true);
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(syncStartResult);
    String errorMessage = TASK_ALREADY_RUNNING_MESSAGE_PATTERN.formatted(track.value());

    TaskAlreadyRunningException taskAlreadyRunningException = assertThrows(TaskAlreadyRunningException.class,
        () -> aspect.aroundSyncTask(pjp, track), "Should throw TaskAlreadyRunningException when execution status is " +
            "RUNNING");
    assertEquals(errorMessage, taskAlreadyRunningException.getMessage(),
        "Exception message should match the thrown message");
    verify(syncTaskExecutionService, never()).markStatusSuccess(any(), any());
    verify(syncTaskExecutionService, never()).markStatusFailure(any(), any());
  }

  private SyncStartResult getSyncStartResult(boolean isAlreadyRunning) {
    SyncTaskExecution execution = new SyncTaskExecution();
    SyncStartResult syncStartResult = new SyncStartResult();
    syncStartResult.setSyncTaskExecution(execution);
    syncStartResult.setAlreadyRunning(isAlreadyRunning);

    return syncStartResult;
  }
}