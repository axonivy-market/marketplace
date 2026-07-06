package com.axonivy.market.service.impl;

import com.axonivy.market.config.SyncTaskCancellationRegistry;
import com.axonivy.market.constants.SyncTaskConstants;
import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.SyncTaskInProgressException;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.repository.SyncTaskExecutionRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncTaskExecutionServiceImplTest {
  private static final String MESSAGE = "Test message";
  private static final String LONG_MESSAGE = StringUtils.repeat("a", 2000);
  private SyncTaskExecutionRepository repo;
  private SyncTaskExecutionServiceImpl service;
  @MockitoBean
  private SyncTaskCancellationRegistry cancellationRegistry;

  @BeforeEach
  void setUp() {
    repo = mock(SyncTaskExecutionRepository.class);
    cancellationRegistry = mock(SyncTaskCancellationRegistry.class);
    // explicitly set nodeNumber to 1 so repository calls use the expected node
    service = SyncTaskExecutionServiceImpl.builder()
        .syncTaskExecutionRepo(repo)
        .cancellationRegistry(cancellationRegistry)
        .nodeNumber(1)
        .build();
  }

  @Test
  void testStartCreatesNewExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.empty());
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
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.of(existing));
    when(repo.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SyncTaskExecution result = service.start(type);
    assertEquals(type, result.getType(), "Type should match the input type");
    assertEquals(SyncTaskStatus.STARTED, result.getStatus(), "Status should be STARTED after start");
    assertEquals(SyncTaskConstants.STARTED_MESSAGE, result.getMessage(),
        "Message should be Sync task has started! after start");
  }

  @Test
  void testStartThrowSyncTaskInProgressExceptionWhenSyncTaskStatusIsRunning() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(
        SyncTaskStatus.RUNNING).build();
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.of(existedSyncTaskExecution));

    assertThrows(SyncTaskInProgressException.class,
        () -> service.start(type), "Should throw SyncTaskInProgressException when execution status is " +
            "RUNNING");
  }

  @Test
  void testStartThrowSyncTaskInProgressExceptionWhenSyncTaskStatusIsStarted() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(
        SyncTaskStatus.STARTED).build();
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.of(existedSyncTaskExecution));

    assertThrows(SyncTaskInProgressException.class,
        () -> service.start(type), "Should throw SyncTaskInProgressException when execution status is " +
            "STARTED");
  }

  @Test
  void testStartThrowSyncTaskInProgressExceptionWhenCreateCollidesWithActiveExecution() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existedSyncTaskExecution = SyncTaskExecution.builder().type(type).status(
        SyncTaskStatus.STARTED).build();
    when(repo.findByTypeAndNodeNumber(type, 1))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(existedSyncTaskExecution));
    when(repo.saveAndFlush(any())).thenThrow(
        new DataIntegrityViolationException("Unique constraint violation while creating sync task execution"));

    assertThrows(SyncTaskInProgressException.class,
        () -> service.start(type), "Should throw SyncTaskInProgressException when another node creates the row");
  }

  @Test
  void testStartRethrowsDataIntegrityViolationExceptionWhenCreateFailsAndNoRowExists() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    DataIntegrityViolationException exception = new DataIntegrityViolationException(
        "Unique constraint violation while creating sync task execution");
    when(repo.findByTypeAndNodeNumber(type, 1))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.empty());
    when(repo.saveAndFlush(any())).thenThrow(exception);

    assertThrows(DataIntegrityViolationException.class,
        () -> service.start(type), "Should rethrow DataIntegrityViolationException when no row can be re-read");
  }

  @Test
  void testStartThrowsSyncTaskInProgressExceptionWhenRestartCollidesWithConcurrentUpdate() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution existingExecution = SyncTaskExecution.builder().type(type).status(SyncTaskStatus.SUCCESS).build();
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.of(existingExecution));
    when(repo.saveAndFlush(any())).thenThrow(new ObjectOptimisticLockingFailureException(SyncTaskExecution.class, type));

    assertThrows(SyncTaskInProgressException.class,
        () -> service.start(type),
        "Should throw SyncTaskInProgressException when another node updates the row concurrently during restart");
  }

  @Test
  void testMarkStatusSuccessDoesNotThrowWhenConcurrentUpdateDetected() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.findByTypeAndNodeNumber(execution.getType(), 1)).thenReturn(Optional.of(execution));
    when(repo.saveAndFlush(any())).thenThrow(new ObjectOptimisticLockingFailureException(SyncTaskExecution.class,
        SyncTaskType.SYNC_PRODUCTS));

    assertDoesNotThrow(() -> service.markStatusSuccess(execution.getType(), MESSAGE),
        "Should silently skip status update when optimistic lock conflict is detected");
  }

  @Test
  void testMarkStatusSuccess() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.findByTypeAndNodeNumber(execution.getType(), 1)).thenReturn(Optional.of(execution));
    when(repo.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution.getType(), MESSAGE);
    assertEquals(SyncTaskStatus.SUCCESS, execution.getStatus(), "Status should be SUCCESS after markStatusSuccess");
    assertNotNull(execution.getCompletedDate(), "CompletedDate should not be null after markStatusSuccess");
    assertEquals(MESSAGE, execution.getMessage(), "Message should match the input message");
  }

  @Test
  void testMarkStatusFailure() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.findByTypeAndNodeNumber(execution.getType(), 1)).thenReturn(Optional.of(execution));
    when(repo.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusFailure(execution.getType(), MESSAGE);
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
    when(repo.findByTypeAndNodeNumber(execution.getType(), 1)).thenReturn(Optional.of(execution));
    when(repo.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusRunning(execution.getType(), MESSAGE);
    assertEquals(SyncTaskStatus.RUNNING, execution.getStatus(), "Status should be RUNNING after markStatusRunning");
    assertNotNull(execution.getLastRunDate(), "LastRunDate should not be null after markStatusRunning");
    assertNull(execution.getCompletedDate(), "CompletedDate should be null after markStatusRunning");
    assertEquals(MESSAGE, execution.getMessage(), "Message should match the input message");
  }

  @Test
  void testMarkStatusMessageIsAbbreviated() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).build();
    when(repo.findByTypeAndNodeNumber(execution.getType(), 1)).thenReturn(Optional.of(execution));
    when(repo.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    service.markStatusSuccess(execution.getType(), LONG_MESSAGE);
    assertTrue(execution.getMessage().length() <= 1024, "Message should be abbreviated to 1024 characters");
  }

  @Test
  void testGetAllSyncTaskExecutionsEmpty() {
    when(repo.findByTypeAndNodeNumber(any(), eq(1))).thenReturn(Optional.empty());
    List<SyncTaskExecutionModel> result = service.getAllSyncTaskExecutions();
    assertTrue(result.isEmpty() || result.stream().allMatch(Objects::isNull), "Result should be empty or all null");
  }

  @Test
  void testGetSyncTaskExecutionByKeyNotFound() {
    when(repo.findByTypeAndNodeNumber(SyncTaskType.SYNC_PRODUCTS, 1)).thenReturn(Optional.empty());
    SyncTaskExecutionModel result = service.getSyncTaskExecutionByKey(SyncTaskType.SYNC_PRODUCTS.getKey());
    assertNull(result, "Result should be null if not found");
  }

  @Test
  void testGetSyncTaskExecutionByKeyInvalidKey() {
    SyncTaskExecutionModel result = service.getSyncTaskExecutionByKey("invalid-key");
    assertNull(result, "Result should be null for invalid key");
  }

  @Test
  void testMarkStatusWithNullTypeDoesNotThrow() {
    // The service handles missing executions gracefully (no NPE) when the type is null
    assertDoesNotThrow(() -> service.markStatusSuccess(null, MESSAGE),
        "Should not throw NullPointerException if execution is null");
  }

  @Test
  void testCancelInvokesCancellationRegistryWhenRunning() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution execution = SyncTaskExecution.builder().type(type).status(SyncTaskStatus.RUNNING).build();
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.of(execution));

    boolean result = service.cancel(type.getKey());

    assertTrue(result, "Cancel should return true when task is running or started");
    verify(cancellationRegistry).cancel(type);
  }

  @Test
  void testCancelReturnsFalseWhenNotActive() {
    SyncTaskType type = SyncTaskType.SYNC_PRODUCTS;
    SyncTaskExecution execution = SyncTaskExecution.builder().type(type).status(SyncTaskStatus.SUCCESS).build();
    when(repo.findByTypeAndNodeNumber(type, 1)).thenReturn(Optional.of(execution));

    boolean result = service.cancel(type.getKey());

    assertFalse(result, "Cancel should return false when task is not running or started");
    verify(cancellationRegistry, never()).cancel(any());
  }

  @Test
  void testInitializeCancellationRegistryMarksRunningTasksFailed() throws Exception {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).status(SyncTaskStatus.RUNNING).build();
    when(repo.findByStatusIn(any())).thenReturn(List.of(execution));
    when(repo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    java.lang.reflect.Method method = SyncTaskExecutionServiceImpl.class.getDeclaredMethod("initializeCancellationRegistry");
    method.setAccessible(true);
    method.invoke(service);

    assertEquals(SyncTaskStatus.FAILED, execution.getStatus(), "Running execution should be marked as FAILED");
    assertEquals("Sync task was interrupted due to application restart.", execution.getMessage(), "Message should be set to restart notice");
    verify(repo).saveAll(any());
  }

  @Test
  void testGetSyncTaskExecutionByKeyFound() {
    SyncTaskExecution execution = SyncTaskExecution.builder().type(SyncTaskType.SYNC_PRODUCTS).status(SyncTaskStatus.SUCCESS).build();
    when(repo.findByTypeAndNodeNumber(SyncTaskType.SYNC_PRODUCTS, 1)).thenReturn(Optional.of(execution));

    SyncTaskExecutionModel model = service.getSyncTaskExecutionByKey(SyncTaskType.SYNC_PRODUCTS.getKey());
    assertNotNull(model, "Model should be returned when execution exists");
    assertEquals(SyncTaskType.SYNC_PRODUCTS.getKey(), model.getKey(), "Model key should match the type key");
  }
}