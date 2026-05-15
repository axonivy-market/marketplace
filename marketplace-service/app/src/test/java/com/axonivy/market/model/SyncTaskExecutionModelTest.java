package com.axonivy.market.model;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SyncTaskExecutionModelTest {
  private static final LocalDateTime MOCK_LOCAL_DATE = LocalDateTime.of(2024, 1, 1, 0, 0);
  private static final String KEY = "key";
  private static final String MESSAGE = "message";
  private static final String DONE = "done";

    @Test
    void testBuilderAndGettersSetters() {
        SyncTaskExecutionModel model = SyncTaskExecutionModel.builder()
                .key(KEY)
                .status(SyncTaskStatus.SUCCESS)
                .lastRunDate(MOCK_LOCAL_DATE)
                .completedDate(MOCK_LOCAL_DATE)
                .message(MESSAGE)
                .build();
        assertEquals(KEY, model.getKey(), "Key should match the input key");
        assertEquals(SyncTaskStatus.SUCCESS, model.getStatus(), "Status should be SUCCESS");
        assertEquals(MOCK_LOCAL_DATE, model.getLastRunDate(), "LastRunDate should match the input date");
        assertEquals(MOCK_LOCAL_DATE, model.getCompletedDate(), "CompletedDate should match the input date");
        assertEquals(MESSAGE, model.getMessage(), "Message should match the input message");
    }

    @Test
    void testNoArgsAndAllArgsConstructors() {
        SyncTaskExecutionModel noArgs = new SyncTaskExecutionModel();
        assertNull(noArgs.getKey(), "Key should be null for no-args constructor");
        assertNull(noArgs.getStatus(), "Status should be null for no-args constructor");
        assertNull(noArgs.getLastRunDate(), "LastRunDate should be null for no-args constructor");
        assertNull(noArgs.getCompletedDate(), "CompletedDate should be null for no-args constructor");
        assertNull(noArgs.getMessage(), "Message should be null for no-args constructor");

        SyncTaskExecutionModel allArgs = new SyncTaskExecutionModel(
                KEY, SyncTaskStatus.FAILED, MOCK_LOCAL_DATE, MOCK_LOCAL_DATE, MESSAGE);
        assertEquals(KEY, allArgs.getKey(), "Key should match the input key");
        assertEquals(SyncTaskStatus.FAILED, allArgs.getStatus(), "Status should be FAILED");
        assertEquals(MOCK_LOCAL_DATE, allArgs.getLastRunDate(), "LastRunDate should match the input date");
        assertEquals(MOCK_LOCAL_DATE, allArgs.getCompletedDate(), "CompletedDate should match the input date");
        assertEquals(MESSAGE, allArgs.getMessage(), "Message should match the input message");
    }

    @Test
    void testFromSyncTaskExecution() {
        SyncTaskExecution execution = new SyncTaskExecution();
        execution.setType(SyncTaskType.SYNC_PRODUCTS);
        execution.setStatus(SyncTaskStatus.SUCCESS);
        execution.setLastRunDate(MOCK_LOCAL_DATE);
        execution.setCompletedDate(MOCK_LOCAL_DATE);
        execution.setMessage(DONE);

        SyncTaskExecutionModel model = SyncTaskExecutionModel.from(execution);
        assertEquals(SyncTaskType.SYNC_PRODUCTS.getKey(), model.getKey(), "Key should match the type's key");
        assertEquals(SyncTaskStatus.SUCCESS, model.getStatus(), "Status should be SUCCESS");
        assertEquals(MOCK_LOCAL_DATE, model.getLastRunDate(), "LastRunDate should match the input date");
        assertEquals(MOCK_LOCAL_DATE, model.getCompletedDate(), "CompletedDate should match the input date");
        assertEquals(DONE, model.getMessage(), "Message should match the input message");
    }
}