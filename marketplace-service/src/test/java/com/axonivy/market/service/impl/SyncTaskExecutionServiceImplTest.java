package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.model.SyncStartResult;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    when(repo.findByType(type)).thenReturn(Optional.empty());
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncStartResult result = service.start(type);
    SyncTaskExecution syncTaskExecution = result.getSyncTaskExecution();
    assertEquals(type, syncTaskExecution.getType(), "Type should match the input type");
    assertEquals(SyncTaskStatus.RUNNING, syncTaskExecution.getStatus(), "Status should be RUNNING after start");
    assertNotNull(syncTaskExecution.getTriggeredAt(), "TriggeredAt should not be null after start");
    assertNull(syncTaskExecution.getCompletedAt(), "CompletedAt should be null after start");
    assertNull(syncTaskExecution.getMessage(), "Message should be null after start");
  }

  @Test
  void testStartUpdatesExistingExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existing = SyncTaskExecution.builder().type(type).build();
    when(repo.findByType(type)).thenReturn(Optional.of(existing));
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncStartResult result = service.start(type);
    SyncTaskExecution syncTaskExecution = result.getSyncTaskExecution();
    assertEquals(type, syncTaskExecution.getType(), "Type should match the input type");
    assertEquals(SyncTaskStatus.RUNNING, syncTaskExecution.getStatus(), "Status should be RUNNING after start");
    assertNotNull(syncTaskExecution.getTriggeredAt(), "TriggeredAt should not be null after start");
    assertNull(syncTaskExecution.getCompletedAt(), "CompletedAt should be null after start");
    assertNull(syncTaskExecution.getMessage(), "Message should be null after start");
  }

  @Test
  void testStartReturnSyncStartResultWithAlreadyRunningStatus() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(SyncTaskStatus.RUNNING).build();
    when(repo.findByType(type)).thenReturn(Optional.of(existedSyncTaskExecution));

    SyncStartResult syncStartResult = service.start(type);
    assertTrue(syncStartResult.isAlreadyRunning(), "SyncStartResult isAlreadyRunning should be true when " +
        "existedSyncTaskExecution status is RUNNING");
  }


  @Test
  void testStartReturnSyncStartResultWithNotAlreadyRunningStatus() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).build();
    when(repo.findByType(type)).thenReturn(Optional.of(existedSyncTaskExecution));

    SyncStartResult syncStartResult = service.start(type);
    assertFalse(syncStartResult.isAlreadyRunning(), "SyncStartResult isAlreadyRunning should be true when " +
        "existedSyncTaskExecution status is null");
  }

  @Test
  void testMarkStatusSuccess() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution, MESSAGE);
    assertEquals(SyncTaskStatus.SUCCESS, execution.getStatus(), "Status should be SUCCESS after markStatusSuccess");
    assertNotNull(execution.getCompletedAt(), "CompletedAt should not be null after markStatusSuccess");
    assertEquals(MESSAGE, execution.getMessage(), "Message should match the input message");
  }

  @Test
  void testMarkStatusFailure() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusFailure(execution, MESSAGE);
    assertEquals(SyncTaskStatus.FAILED, execution.getStatus(), "Status should be FAILED after markStatusFailure");
    assertNotNull(execution.getCompletedAt(), "CompletedAt should not be null after markStatusFailure");
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