package com.axonivy.market.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class LogStreamRegistryTest {
  private static final String TASK_KEY = "syncProducts";
  private static final String ANOTHER_TASK_KEY = "syncGithubMonitor";

  @Test
  void testAsFluxReturnsNonNullFlux() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    assertNotNull(flux, "LogStreamRegistry.asFlux() should return a non-null Flux");
  }

  @Test
  void testHasSubscribersReturnsTrueWhenSubscribed() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    flux.subscribe(value -> {
    }, error -> {
    }, () -> {
    });
    // After subscribing, should have subscribers
    boolean afterSubscriptionHasSubscribers = LogStreamRegistry.hasSubscribers();
    assertTrue(afterSubscriptionHasSubscribers, "LogStreamRegistry should have subscribers after subscription");
  }

  @Test
  void testPushEmitsLogLine() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    String testMessage = "Test log message";
    List<String> received = new ArrayList<>();
    flux.subscribe(received::add);
    LogStreamRegistry.push(testMessage);
    // Wait for async processing
    await().atMost(5, TimeUnit.SECONDS).until(() -> received.contains(testMessage));
    assertTrue(received.contains(testMessage), "Pushed message should be received by the subscriber");
  }

  @Test
  void testPushMultipleLogLines() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    String message1 = "Log message 1";
    String message2 = "Log message 2";
    String message3 = "Log message 3";
    List<String> received = new ArrayList<>();
    flux.subscribe(received::add);
    LogStreamRegistry.push(message1);
    LogStreamRegistry.push(message2);
    LogStreamRegistry.push(message3);
    await().atMost(5, TimeUnit.SECONDS).until(() -> received.size() >= 3);
    assertTrue(received.contains(message1), "First message should be received");
    assertTrue(received.contains(message2), "Second message should be received");
    assertTrue(received.contains(message3), "Third message should be received");
  }

  @Test
  void testPushWithEmptyString() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    String emptyMessage = "";
    List<String> received = new ArrayList<>();
    flux.subscribe(received::add);
    LogStreamRegistry.push(emptyMessage);
    await().atMost(5, TimeUnit.SECONDS).until(() -> received.contains(emptyMessage));
    assertTrue(received.contains(emptyMessage), "Empty string message should be received");
  }

  @Test
  void testPushWithMultipleSubscribers() {
    String testMessage = "Multicast test message";
    List<String> subscriber1Records = new ArrayList<>();
    List<String> subscriber2Records = new ArrayList<>();
    Flux<String> flux = LogStreamRegistry.asFlux();
    flux.subscribe(subscriber1Records::add);
    flux.subscribe(subscriber2Records::add);
    LogStreamRegistry.push(testMessage);
    await().atMost(5, TimeUnit.SECONDS).until(
        () -> subscriber1Records.contains(testMessage) && subscriber2Records.contains(testMessage));
    assertTrue(subscriber1Records.contains(testMessage), "First subscriber should receive the message");
    assertTrue(subscriber2Records.contains(testMessage), "Second subscriber should receive the message");
  }

  @Test
  void testFluxIsMulticast() {
    String message = "Multicast message";
    Flux<String> flux = LogStreamRegistry.asFlux();
    List<String> received1 = new ArrayList<>();
    List<String> received2 = new ArrayList<>();
    flux.subscribe(received1::add);
    LogStreamRegistry.push(message);
    flux.subscribe(received2::add);
    LogStreamRegistry.push(message);
    await().atMost(5, TimeUnit.SECONDS).until(() -> received1.size() >= 1 && received2.size() >= 1);
    assertTrue(received1.size() >= 1, "First subscriber should receive at least one message");
    assertTrue(received2.size() >= 1, "Second subscriber should receive at least one message");
  }

  @Test
  void testPushLogFailure(CapturedOutput output) {
    Object originalSink = ReflectionTestUtils.getField(LogStreamRegistry.class, "sink");
    Sinks.Many<String> mockSink = org.mockito.Mockito.mock(Sinks.Many.class);
    org.mockito.Mockito.when(mockSink.tryEmitNext(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(Sinks.EmitResult.FAIL_TERMINATED);
    org.mockito.Mockito.when(mockSink.currentSubscriberCount()).thenReturn(0);

    try {
      ReflectionTestUtils.setField(LogStreamRegistry.class, "sink", mockSink);

      LogStreamRegistry.push("Test Failure");

      assertTrue(output.getOut().contains("Failed to emit log line to stream registry"),
          "Should log warning on failure");
    } finally {
      ReflectionTestUtils.setField(LogStreamRegistry.class, "sink", originalSink);
    }
  }

  @Test
  void testAsFluxByTaskKeyReturnsNonNull() {
    Flux<String> flux = LogStreamRegistry.asFlux(TASK_KEY);
    assertNotNull(flux, "asFlux(taskKey) should return non-null");
  }

  @Test
  void testAsFluxByTaskKeyReturnsReplayLogs() {
    LogStreamRegistry.resetTask(TASK_KEY);

    LogStreamRegistry.pushTask(TASK_KEY, "line 1");
    LogStreamRegistry.pushTask(TASK_KEY, "line 2");

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertEquals(List.of("line 1", "line 2"), collected, "Expected collected logs to match the pushed lines in correct order");
  }

  @Test
  void testAsFluxByTaskKeyReturnsEmptyWhenNoData() {
    LogStreamRegistry.resetTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertNotNull(collected, "No logs");
    assertTrue(collected.isEmpty(), "Expected no logs but some were found");
  }

  @Test
  void testAsFluxByTaskKeyReplayAndLive() {
    LogStreamRegistry.resetTask(TASK_KEY);

    LogStreamRegistry.pushTask(TASK_KEY, "buffered line");

    List<String> received = new ArrayList<>();

    LogStreamRegistry.asFlux(TASK_KEY)
        .subscribe(received::add);

    LogStreamRegistry.pushTask(TASK_KEY, "live line");

    await().atMost(5, TimeUnit.SECONDS)
        .until(() -> received.size() >= 2);

    assertEquals(List.of("buffered line", "live line"), received, "Expected to receive buffered log first, then live log in order");
  }

  @Test
  void testAsFluxByTaskKeyIsolatedPerKey() {
    LogStreamRegistry.resetTask(TASK_KEY);
    LogStreamRegistry.resetTask(ANOTHER_TASK_KEY);

    LogStreamRegistry.pushTask(TASK_KEY, "log for syncProducts");
    LogStreamRegistry.pushTask(ANOTHER_TASK_KEY, "log for syncGithubMonitor");

    List<String> collectedA = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));
    List<String> collectedB = LogStreamRegistry.asFlux(ANOTHER_TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertNotNull(
        collectedA,
        "Collected logs for task A (syncProducts) should not be null"
    );

    assertTrue(
        collectedA.contains("log for syncProducts"),
        "Task A should contain its own log entry"
    );

    assertFalse(
        collectedA.contains("log for syncGithubMonitor"),
        "Task A should not contain Task B logs"
    );

    assertNotNull(
        collectedB,
        "Collected logs for task B (syncGithubMonitor) should not be null"
    );

    assertTrue(
        collectedB.contains("log for syncGithubMonitor"),
        "Task B should contain its own log entry"
    );

    assertFalse(
        collectedB.contains("log for syncProducts"),
        "Task B should not contain Task A logs"
    );
  }

  @Test
  void testPushTaskEmitsToSubscriber() {
    LogStreamRegistry.resetTask(TASK_KEY);

    List<String> result = new ArrayList<>();

    LogStreamRegistry.asFlux(TASK_KEY)
        .subscribe(result::add);

    LogStreamRegistry.pushTask(TASK_KEY, "pushed line");

    await().atMost(5, TimeUnit.SECONDS)
        .until(() -> !result.isEmpty());

    assertEquals(List.of("pushed line"), result, "Expected the pushed line to be received by subscriber");
  }

  @Test
  void testPushTaskPreservesOrder() {
    LogStreamRegistry.resetTask(TASK_KEY);

    List<String> lines = List.of("line 1", "line 2", "line 3");
    lines.forEach(l -> LogStreamRegistry.pushTask(TASK_KEY, l));

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertEquals(lines, collected, "Expected collected logs to match input lines in the same order");
  }

  @Test
  void testCompleteTaskCompletesFlux() {
    LogStreamRegistry.resetTask(TASK_KEY);

    Flux<String> flux = LogStreamRegistry.asFlux(TASK_KEY);
    List<String> received = new ArrayList<>();
    List<Boolean> completed = new ArrayList<>();

    flux.subscribe(received::add, err -> {
    }, () -> completed.add(true));

    LogStreamRegistry.pushTask(TASK_KEY, "before complete");
    LogStreamRegistry.completeTask(TASK_KEY);

    await().atMost(5, TimeUnit.SECONDS).until(() -> !completed.isEmpty());
    assertFalse(completed.isEmpty(), "Flux should complete after completeTask");
  }

  @Test
  void testCompleteTaskDoesNotRemoveSink() {
    LogStreamRegistry.resetTask(TASK_KEY);
    LogStreamRegistry.pushTask(TASK_KEY, "line");
    LogStreamRegistry.completeTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));
    assertNotNull(collected, "Stream result should not be null");
    assertFalse(collected.isEmpty(), "Replay data should still exist after completeTask");
  }

  @Test
  void testCompleteTaskIsIdempotent() {
    LogStreamRegistry.resetTask(TASK_KEY);

    assertDoesNotThrow(() -> {
      LogStreamRegistry.completeTask(TASK_KEY);
      LogStreamRegistry.completeTask(TASK_KEY);
    }, "Calling completeTask twice should not throw any exception");
  }

  @Test
  void testResetTaskClearsBuffer() {
    LogStreamRegistry.pushTask(TASK_KEY, "should be cleared");
    LogStreamRegistry.resetTask(TASK_KEY);
    LogStreamRegistry.completeTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));
    assertNotNull(collected, "Stream result should not be null");
    assertTrue(collected.isEmpty(), "Buffer should be empty after resetTask");
  }

  @Test
  void testResetTaskCompletesExistingSink() {
    LogStreamRegistry.resetTask(TASK_KEY);

    Flux<String> flux = LogStreamRegistry.asFlux(TASK_KEY);
    List<Boolean> completed = new ArrayList<>();
    flux.subscribe(v -> {
    }, err -> {
    }, () -> completed.add(true));

    LogStreamRegistry.resetTask(TASK_KEY);

    await().during(500, TimeUnit.MILLISECONDS)
        .atMost(1, TimeUnit.SECONDS)
        .untilAsserted(() -> assertTrue(completed.isEmpty(), "Expected no completed tasks but found some"));
    assertTrue(completed.isEmpty(), "resetTask should NOT complete the sink anymore");
  }

  @Test
  void testResetTaskIsIdempotent() {
    assertDoesNotThrow(() -> {
      LogStreamRegistry.resetTask(TASK_KEY);
      LogStreamRegistry.resetTask(TASK_KEY);
    }, "Calling resetTask twice should not throw any exception");
  }

  @Test
  void testPushDoesNotLogWhenNoSubscriber(CapturedOutput output) {
    Object originalSink = ReflectionTestUtils.getField(LogStreamRegistry.class, "sink");

    Sinks.Many<String> mockSink = Mockito.mock(Sinks.Many.class);
    Mockito.when(mockSink.tryEmitNext(ArgumentMatchers.anyString()))
        .thenReturn(Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER);
    Mockito.when(mockSink.currentSubscriberCount()).thenReturn(0);

    try {
      ReflectionTestUtils.setField(LogStreamRegistry.class, "sink", mockSink);

      LogStreamRegistry.push("no subscriber");
      assertFalse(
          output.getOut().contains("Failed to emit log line to stream registry"),
          "Should not log warning when there are no subscribers"
      );
    } finally {
      ReflectionTestUtils.setField(LogStreamRegistry.class, "sink", originalSink);
    }

  }

  @Test
  void testReplayLimit() {
    LogStreamRegistry.resetTask(TASK_KEY);

    for (int i = 0; i < 1100; i++) {
      LogStreamRegistry.pushTask(TASK_KEY, "line " + i);
    }

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(500))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertNotNull(collected, "No logs");
    assertEquals(500, collected.size(), "Expected replay buffer to contain exactly 500 log lines");
    assertEquals("line 600", collected.getFirst(), "Expected oldest retained log to be 'line 600' after applying " +
        "buffer limit");
  }

  @Test
  void testConcurrentPushTask() {
    LogStreamRegistry.resetTask(TASK_KEY);

    int threads = 10;
    int perThread = 50;

   ExecutorService executor = Executors.newFixedThreadPool(threads);

    for (int t = 0; t < threads; t++) {
      int threadId = t;
      executor.submit(() -> {
        for (int i = 0; i < perThread; i++) {
          LogStreamRegistry.pushTask(TASK_KEY, "t" + threadId + "-" + i);
        }
      });
    }

    executor.shutdown();

    await().atMost(5, TimeUnit.SECONDS)
        .until(executor::isTerminated);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(500))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertNotNull(collected, "No logs");
    assertTrue(collected.size() >= threads * perThread * 0.8, "Expected at least 80% of logs to be collected under concurrent push");
  }

  @Test
  void testSubscribeAfterCompleteKeepsReplayData() {
    LogStreamRegistry.resetTask(TASK_KEY);

    LogStreamRegistry.pushTask(TASK_KEY, "line");
    LogStreamRegistry.completeTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertNotNull(collected, "No logs");
    assertEquals(List.of("line"), collected, "Replay data should still be available after completeTask is called");
  }

  @Test
  void testPushTaskRetriesWhenTerminated() {
    LogStreamRegistry.resetTask(TASK_KEY);

    Sinks.Many<String> mockSink = Mockito.mock(Sinks.Many.class);
    Mockito.when(mockSink.tryEmitNext("retry-line")).thenReturn(Sinks.EmitResult.FAIL_TERMINATED);

    @SuppressWarnings("unchecked")
    Map<String, Sinks.Many<String>> map =
        (Map<String, Sinks.Many<String>>) ReflectionTestUtils
            .getField(LogStreamRegistry.class, "taskSinks");

    map.put(TASK_KEY, mockSink);

    LogStreamRegistry.pushTask(TASK_KEY, "retry-line");
    List<String> received = new ArrayList<>();
    LogStreamRegistry.asFlux(TASK_KEY).subscribe(received::add);

    assertEquals(List.of("retry-line"), received, "Expected message to be retried and delivered after FAIL_TERMINATED");
  }

  @Test
  void testCompleteTaskWithoutExistingSink() {
    LogStreamRegistry.resetTask(TASK_KEY);

    assertDoesNotThrow(() -> LogStreamRegistry.completeTask(TASK_KEY),
        "Calling completeTask on a non-existing task should be safe and not throw");
  }

  @Test
  void testPushAfterUnsubscribe() {
    Flux<String> flux = LogStreamRegistry.asFlux();

    var disposable = flux.subscribe();
    disposable.dispose();

    assertDoesNotThrow(() -> LogStreamRegistry.push("after dispose"),
        "Pushing log after unsubscribe should be safe and not throw any exception");
  }

  @Test
  void testResetRemovesBufferCompletely() {
    LogStreamRegistry.pushTask(TASK_KEY, "line");
    LogStreamRegistry.resetTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY)
        .take(Duration.ofMillis(200))
        .collectList()
        .block(Duration.ofSeconds(2));

    assertNotNull(collected, "No logs");
    assertTrue(collected.isEmpty(), "Expected no logs but some were found");
  }
}
