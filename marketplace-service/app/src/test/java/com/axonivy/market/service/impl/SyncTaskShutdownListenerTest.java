package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import com.axonivy.market.service.SyncTaskExecutionService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SyncTaskShutdownListenerTest {
  @Mock
  private SyncTaskExecutionRepository syncTaskExecutionRepo;

  @Mock
  private SyncTaskExecutionService syncTaskExecutionService;

  @InjectMocks
  private SyncTaskShutdownListener listener;

  @Test
  void shouldMarkRunningExecutionAsFailed() {
    SyncTaskExecution runningExecution = mock(SyncTaskExecution.class);
    when(runningExecution.getStatus()).thenReturn(SyncTaskStatus.RUNNING);

    when(syncTaskExecutionRepo.findByType(any()))
        .thenReturn(Optional.of(runningExecution));

    listener.onShutdown();
    verify(syncTaskExecutionService, atLeastOnce())
        .markStatusFailure(
            eq(runningExecution),
            eq("Application shutdown during execution")
        );
  }

  @Test
  void shouldNotMarkNonRunningExecution() {
    // Arrange
    SyncTaskExecution finishedExecution = mock(SyncTaskExecution.class);
    when(finishedExecution.getStatus()).thenReturn(SyncTaskStatus.SUCCESS);

    when(syncTaskExecutionRepo.findByType(any()))
        .thenReturn(Optional.of(finishedExecution));

    listener.onShutdown();
    verify(syncTaskExecutionService, never())
        .markStatusFailure(any(), any());
  }

  @Test
  void shouldContinueWhenDataAccessExceptionThrown() {
    SyncTaskExecution runningExecution = mock(SyncTaskExecution.class);
    when(runningExecution.getStatus()).thenReturn(SyncTaskStatus.RUNNING);
    when(runningExecution.getType()).thenReturn(SyncTaskType.values()[0]);

    when(syncTaskExecutionRepo.findByType(any()))
        .thenReturn(Optional.of(runningExecution));

    doThrow(mock(DataAccessException.class))
        .when(syncTaskExecutionService)
        .markStatusFailure(any(), any());

    assertDoesNotThrow(
        () -> listener.onShutdown(),
        "Listener must not propagate DataAccessException"
    );
  }
}
