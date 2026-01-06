package com.axonivy.market.service.impl;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
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
  void testStart_createsNewExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    when(repo.findByType(type)).thenReturn(Optional.empty());
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncTaskExecution result = service.start(type);
    assertEquals(type, result.getType());
    assertEquals(SyncTaskStatus.RUNNING, result.getStatus());
    assertNotNull(result.getTriggeredAt());
    assertNull(result.getCompletedAt());
    assertNull(result.getMessage());
  }

  @Test
  void testStart_updatesExistingExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existing = SyncTaskExecution.builder().type(type).build();
    when(repo.findByType(type)).thenReturn(Optional.of(existing));
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncTaskExecution result = service.start(type);
    assertEquals(type, result.getType());
    assertEquals(SyncTaskStatus.RUNNING, result.getStatus());
    assertNotNull(result.getTriggeredAt());
    assertNull(result.getCompletedAt());
    assertNull(result.getMessage());
  }

  @Test
  void testMarkStatusSuccess() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution, MESSAGE);
    assertEquals(SyncTaskStatus.SUCCESS, execution.getStatus());
    assertNotNull(execution.getCompletedAt());
    assertEquals(MESSAGE, execution.getMessage());
  }

  @Test
  void testMarkStatusFailure() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusFailure(execution, MESSAGE);
    assertEquals(SyncTaskStatus.FAILED, execution.getStatus());
    assertNotNull(execution.getCompletedAt());
    assertEquals(MESSAGE, execution.getMessage());
  }

  @Test
  void testMarkStatusMessageIsAbbreviated() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution, LONG_MESSAGE);
    assertTrue(execution.getMessage().length() <= 1024);
  }

  @Test
  void testGetAllSyncTaskExecutions_empty() {
    when(repo.findByType(any())).thenReturn(Optional.empty());
    List<SyncTaskExecutionModel> result = service.getAllSyncTaskExecutions();
    assertTrue(result.isEmpty() || result.stream().allMatch(Objects::isNull));
  }

  @Test
  void testGetSyncTaskExecutionByKey_notFound() {
    when(repo.findByType(SyncTaskType.SYNC_PRODUCTS)).thenReturn(Optional.empty());
    SyncTaskExecutionModel result = service.getSyncTaskExecutionByKey(SyncTaskType.SYNC_PRODUCTS.getKey());
    assertNull(result);
  }

  @Test
  void testGetSyncTaskExecutionByKey_invalidKey() {
    SyncTaskExecutionModel result = service.getSyncTaskExecutionByKey("invalid-key");
    assertNull(result);
  }

  @Test
  void testUpdateSyncTask_nullExecutionThrows() {
    assertThrows(NullPointerException.class, () -> {
      service.markStatusSuccess(null, MESSAGE);
    });
  }
}