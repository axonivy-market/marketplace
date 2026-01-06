package com.axonivy.market.model;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SyncTaskExecutionModelTest {
  private static final LocalDate MOCK_LOCAL_DATE = LocalDate.of(2022, 5, 6);
  private static final String KEY = "key";
  private static final String MESSAGE = "message";
  private static final String DONE = "done";

    @Test
    void testBuilderAndGettersSetters() {
        SyncTaskExecutionModel model = SyncTaskExecutionModel.builder()
                .key(KEY)
                .status(SyncTaskStatus.SUCCESS)
                .triggeredAt(MOCK_LOCAL_DATE)
                .completedAt(MOCK_LOCAL_DATE)
                .message(MESSAGE)
                .build();
        assertEquals(KEY, model.getKey());
        assertEquals(SyncTaskStatus.SUCCESS, model.getStatus());
        assertEquals(MOCK_LOCAL_DATE, model.getTriggeredAt());
        assertEquals(MOCK_LOCAL_DATE, model.getCompletedAt());
        assertEquals(MESSAGE, model.getMessage());
    }

    @Test
    void testNoArgsAndAllArgsConstructors() {
        SyncTaskExecutionModel noArgs = new SyncTaskExecutionModel();
        assertNull(noArgs.getKey());
        assertNull(noArgs.getStatus());
        assertNull(noArgs.getTriggeredAt());
        assertNull(noArgs.getCompletedAt());
        assertNull(noArgs.getMessage());

        SyncTaskExecutionModel allArgs = new SyncTaskExecutionModel(
                KEY, SyncTaskStatus.FAILED, MOCK_LOCAL_DATE, MOCK_LOCAL_DATE, MESSAGE);
        assertEquals(KEY, allArgs.getKey());
        assertEquals(SyncTaskStatus.FAILED, allArgs.getStatus());
        assertEquals(MOCK_LOCAL_DATE, allArgs.getTriggeredAt());
        assertEquals(MOCK_LOCAL_DATE, allArgs.getCompletedAt());
        assertEquals(MESSAGE, allArgs.getMessage());
    }

    @Test
    void testFromSyncTaskExecution() {
        SyncTaskExecution execution = new SyncTaskExecution();
        execution.setType(SyncTaskType.SYNC_PRODUCTS);
        execution.setStatus(SyncTaskStatus.SUCCESS);
        execution.setTriggeredAt(MOCK_LOCAL_DATE);
        execution.setCompletedAt(MOCK_LOCAL_DATE);
        execution.setMessage(DONE);

        SyncTaskExecutionModel model = SyncTaskExecutionModel.from(execution);
        assertEquals(SyncTaskType.SYNC_PRODUCTS.getKey(), model.getKey());
        assertEquals(SyncTaskStatus.SUCCESS, model.getStatus());
        assertEquals(MOCK_LOCAL_DATE, model.getTriggeredAt());
        assertEquals(MOCK_LOCAL_DATE, model.getCompletedAt());
        assertEquals(DONE, model.getMessage());
    }
}