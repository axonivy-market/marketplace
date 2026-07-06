package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import com.axonivy.market.service.SyncTaskExecutionService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SyncTaskShutdownListenerTest {
  @Mock
  private SyncTaskExecutionRepository syncTaskExecutionRepo;

  @Mock
  private SyncTaskExecutionService syncTaskExecutionService;

  @InjectMocks
  private SyncTaskShutdownListener listener;

  @Test
  void testMarkRunningExecutionAsFailed() {
    SyncTaskExecution runningExecution = mock(SyncTaskExecution.class);
    when(syncTaskExecutionRepo.findByStatusIn(any()))
        .thenReturn(List.of(runningExecution));

    listener.onShutdown();
    verify(syncTaskExecutionService, atLeastOnce())
        .markStatusFailure(
            runningExecution.getType(),
            "Application shutdown during execution"
        );
  }

  @Test
  void testShouldNotMarkNonRunningExecution() {
    when(syncTaskExecutionRepo.findByStatusIn(any()))
        .thenReturn(List.of());

    listener.onShutdown();
    verify(syncTaskExecutionService, never())
        .markStatusFailure(any(), any());
  }

  @Test
  void testShouldMarkStartedExecutionAsFailed() {
    SyncTaskExecution startedExecution = mock(SyncTaskExecution.class);
    SyncTaskType startedType = SyncTaskType.values()[0];
    when(startedExecution.getType()).thenReturn(startedType);

    when(syncTaskExecutionRepo.findByStatusIn(any()))
        .thenReturn(List.of(startedExecution));

    listener.onShutdown();
    verify(syncTaskExecutionService, times(1))
        .markStatusFailure(startedType, "Application shutdown during execution");
  }

  @Test
  void testShouldContinueWhenDataAccessExceptionThrown() {
    SyncTaskExecution runningExecution = mock(SyncTaskExecution.class);
    when(runningExecution.getType()).thenReturn(SyncTaskType.values()[0]);

    when(syncTaskExecutionRepo.findByStatusIn(any()))
        .thenReturn(List.of(runningExecution));

    doThrow(mock(DataAccessException.class))
        .when(syncTaskExecutionService)
        .markStatusFailure(any(), any());

    assertDoesNotThrow(
        () -> listener.onShutdown(),
        "Listener must not propagate DataAccessException"
    );
  }
}
