package com.axonivy.market.logging;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskType;
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
    SyncTaskExecution execution = new SyncTaskExecution();
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(execution);
    when(pjp.proceed()).thenReturn("result");

    Object result = aspect.aroundSyncTask(pjp, track);

    assertEquals("result", result, "Result should match the value returned by pjp.proceed()");
    verify(syncTaskExecutionService).markStatusSuccess(execution, "Sync successfully!");
    verify(syncTaskExecutionService, never()).markStatusFailure(any(), any());
  }

  @Test
  void testAroundSyncTaskFailure() throws Throwable {
    SyncTaskExecution execution = new SyncTaskExecution();
    when(track.value()).thenReturn(SyncTaskType.SYNC_PRODUCTS);
    when(syncTaskExecutionService.start(SyncTaskType.SYNC_PRODUCTS)).thenReturn(execution);
    String FAILED_MESSAGE = "fail";
    when(pjp.proceed()).thenThrow(new RuntimeException(FAILED_MESSAGE));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> aspect.aroundSyncTask(pjp, track), "Should throw RuntimeException when pjp.proceed() fails");
    assertEquals(FAILED_MESSAGE, thrown.getMessage(), "Exception message should match the thrown message");
    verify(syncTaskExecutionService).markStatusFailure(execution, FAILED_MESSAGE);
    verify(syncTaskExecutionService, never()).markStatusSuccess(any(), any());
  }
}