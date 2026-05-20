package com.axonivy.market.service.impl;

import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.TaskAlreadyRunningException;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncTaskExecutionServiceImplTest {
  private static final String MESSAGE = "Test message";
  private static final String LONG_MESSAGE = StringUtils.repeat("a", 2000);
  private SyncTaskExecutionRepository repo;
  private SyncTaskExecutionServiceImpl service;

  @BeforeEach
  void setUp() {
    repo = mock(SyncTaskExecutionRepository.class);
    service = SyncTaskExecutionServiceImpl.builder().syncTaskExecutionRepo(repo).build();
  }

  @Test
  void testStartCreatesNewExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    when(repo.findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(type)).thenReturn(List.of());
    when(repo.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncTaskExecution result = service.start(type);
    assertEquals(type, result.getType(), "Type should match the input type");
    assertEquals(SyncTaskStatus.STARTED, result.getStatus(), "Status should be STARTED after start");
    assertEquals(SyncTaskConstants.STARTED_MESSAGE, result.getMessage(),
        "Message should be Sync task has started! after start");
  }

  @Test
  void testStartUpdatesExistingExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existing = SyncTaskExecution.builder().type(type).build();
    when(repo.findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(type)).thenReturn(List.of(existing));
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncTaskExecution result = service.start(type);
    assertEquals(type, result.getType(), "Type should match the input type");
    assertEquals(SyncTaskStatus.STARTED, result.getStatus(), "Status should be STARTED after start");
    assertEquals(SyncTaskConstants.STARTED_MESSAGE, result.getMessage(),
        "Message should be Sync task has started! after start");
  }

  @Test
  void testStartThrowTaskAlreadyRunningExceptionWhenSyncTaskStatusIsRunning() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(
        SyncTaskStatus.RUNNING).build();
    when(repo.findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(type)).thenReturn(List.of(existedSyncTaskExecution));

    assertThrows(TaskAlreadyRunningException.class,
        () -> service.start(type), "Should throw TaskAlreadyRunningException when execution status is " +
            "RUNNING");
  }

    @Test
    void testStartThrowTaskAlreadyRunningExceptionWhenSyncTaskStatusIsStarted() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(
      SyncTaskStatus.STARTED).build();
    when(repo.findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(type)).thenReturn(List.of(existedSyncTaskExecution));

    assertThrows(TaskAlreadyRunningException.class,
      () -> service.start(type), "Should throw TaskAlreadyRunningException when execution status is " +
        "STARTED");
    }

    @Test
    void testStartThrowTaskAlreadyRunningExceptionWhenCreateCollidesWithActiveExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(
      SyncTaskStatus.STARTED).build();
    when(repo.findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(type))
      .thenReturn(List.of())
      .thenReturn(List.of(existedSyncTaskExecution));
    when(repo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThrows(TaskAlreadyRunningException.class,
      () -> service.start(type), "Should throw TaskAlreadyRunningException when another node creates the row");
    }

  @Test
  void testMarkStatusSuccess() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution, MESSAGE);
    assertEquals(SyncTaskStatus.SUCCESS, execution.getStatus(), "Status should be SUCCESS after markStatusSuccess");
    assertNotNull(execution.getCompletedDate(), "CompletedDate should not be null after markStatusSuccess");
    assertEquals(MESSAGE, execution.getMessage(), "Message should match the input message");
  }

  @Test
  void testMarkStatusFailure() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusFailure(execution, MESSAGE);
    assertEquals(SyncTaskStatus.FAILED, execution.getStatus(), "Status should be FAILED after markStatusFailure");
    assertNotNull(execution.getCompletedDate(), "CompletedDate should not be null after markStatusFailure");
    assertEquals(MESSAGE, execution.getMessage(), "Message should match the input message");
  }

  @Test
  void testMarkStatusRunning() {
    SyncTaskExecution execution = SyncTaskExecution.builder()
        .type(SyncTaskType.SYNC_PRODUCTS)
        .completedDate(LocalDateTime.now())
        .build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusRunning(execution, MESSAGE);
    assertEquals(SyncTaskStatus.RUNNING, execution.getStatus(), "Status should be RUNNING after markStatusRunning");
    assertNotNull(execution.getLastRunDate(), "LastRunDate should not be null after markStatusRunning");
    assertNull(execution.getCompletedDate(), "CompletedDate should be null after markStatusRunning");
    assertEquals(MESSAGE, execution.getMessage(), "Message should match the input message");
  }

  @Test
  void testMarkStatusMessageIsAbbreviated() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution, LONG_MESSAGE);
    assertTrue(execution.getMessage().length() <= 1024, "Message should be abbreviated to 1024 characters");
  }

  @Test
  void testGetAllSyncTaskExecutionsEmpty() {
    when(repo.findByType(any())).thenReturn(Optional.empty());
    List<SyncTaskExecutionModel> result = service.getAllSyncTaskExecutions();
    assertTrue(result.isEmpty() || result.stream().allMatch(Objects::isNull), "Result should be empty or all null");
  }

  @Test
  void testGetSyncTaskExecutionByKeyNotFound() {
    when(repo.findByType(SyncTaskType.SYNC_PRODUCTS)).thenReturn(Optional.empty());
    SyncTaskExecutionModel result = service.getSyncTaskExecutionByKey(SyncTaskType.SYNC_PRODUCTS.getKey());
    assertNull(result, "Result should be null if not found");
  }

  @Test
  void testGetSyncTaskExecutionByKeyInvalidKey() {
    SyncTaskExecutionModel result = service.getSyncTaskExecutionByKey("invalid-key");
    assertNull(result, "Result should be null for invalid key");
  }

  @Test
  void testUpdateSyncTaskNullExecutionThrows() {
    assertThrows(NullPointerException.class, () -> service.markStatusSuccess(null, MESSAGE),
        "Should throw NullPointerException if execution is null");
  }
}