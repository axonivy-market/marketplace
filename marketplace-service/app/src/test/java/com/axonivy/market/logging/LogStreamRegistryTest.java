package com.axonivy.market.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
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
  void testAsFluxByTaskKeyReturnsBufferedLogsWhenNoSinkExists() {
    LogStreamRegistry.resetTask(TASK_KEY);

    // Push directly to buffer via pushTask then complete to remove sink
    LogStreamRegistry.pushTask(TASK_KEY, "buffered line 1");
    LogStreamRegistry.pushTask(TASK_KEY, "buffered line 2");
    LogStreamRegistry.completeTask(TASK_KEY); // removes sink, keeps buffer

    Flux<String> flux = LogStreamRegistry.asFlux(TASK_KEY);
    List<String> collected = flux.collectList().block();

    assertNotNull(collected, "Stream result should not be null");
    assertTrue(collected.contains("buffered line 1"), "Should return buffered line 1");
    assertTrue(collected.contains("buffered line 2"), "Should return buffered line 2");
  }

  @Test
  void testAsFluxByTaskKeyReturnsEmptyWhenNoBufferAndNoSink() {
    LogStreamRegistry.resetTask(TASK_KEY);

    Flux<String> flux = LogStreamRegistry.asFlux(TASK_KEY);
    List<String> collected = flux.collectList().block();

    assertNotNull(collected, "Stream result should not be null");
    assertTrue(collected.isEmpty(), "Should return empty when no buffer and no sink");
  }

  @Test
  void testAsFluxByTaskKeyConcatsBufferWithLive() {
    LogStreamRegistry.resetTask(TASK_KEY);

    // Push first to build buffer, keep sink alive
    LogStreamRegistry.pushTask(TASK_KEY, "buffered line");

    Flux<String> flux = LogStreamRegistry.asFlux(TASK_KEY);
    List<String> received = new ArrayList<>();
    flux.subscribe(received::add);

    // Push live after subscribe
    LogStreamRegistry.pushTask(TASK_KEY, "live line");

    await().atMost(5, TimeUnit.SECONDS)
        .until(() -> received.contains("buffered line") && received.contains("live line"));

    assertTrue(received.contains("buffered line"), "Should contain buffered line");
    assertTrue(received.contains("live line"), "Should contain live line");
  }

  @Test
  void testAsFluxByTaskKeyIsolatedPerKey() {
    LogStreamRegistry.resetTask(TASK_KEY);
    LogStreamRegistry.resetTask(ANOTHER_TASK_KEY);

    LogStreamRegistry.pushTask(TASK_KEY, "log for syncProducts");
    LogStreamRegistry.pushTask(ANOTHER_TASK_KEY, "log for syncGithubMonitor");
    LogStreamRegistry.completeTask(TASK_KEY);
    LogStreamRegistry.completeTask(ANOTHER_TASK_KEY);

    List<String> collectedA = LogStreamRegistry.asFlux(TASK_KEY).collectList().block();
    List<String> collectedB = LogStreamRegistry.asFlux(ANOTHER_TASK_KEY).collectList().block();

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

    LogStreamRegistry.pushTask(TASK_KEY, "pushed line");

    List<String> result = LogStreamRegistry.asFlux(TASK_KEY)
        .take(1)
        .collectList()
        .block();

    assertEquals(
        List.of("pushed line"),
        result,
        "Result should contain exactly one pushed line"
    );
  }

  @Test
  void testPushTaskAddsToBuffer() {
    LogStreamRegistry.resetTask(TASK_KEY);

    LogStreamRegistry.pushTask(TASK_KEY, "buffered");
    LogStreamRegistry.completeTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY).collectList().block();
    assertNotNull(collected, "Stream result should not be null");
    assertTrue(collected.contains("buffered"), "pushTask should add line to buffer");
  }

  @Test
  void testPushTaskMultipleLinesPreservesOrder() {
    LogStreamRegistry.resetTask(TASK_KEY);

    List<String> lines = List.of("line 1", "line 2", "line 3");
    lines.forEach(l -> LogStreamRegistry.pushTask(TASK_KEY, l));
    LogStreamRegistry.completeTask(TASK_KEY);

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY).collectList().block();
    assertNotNull(collected, "Stream result should not be null");
    assertEquals(lines.size(), collected.size(),
        "Collected list size should match the number of input lines");
    assertEquals(lines, collected, "Buffer order should be preserved");
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
  void testCompleteTaskRemovesSink() {
    LogStreamRegistry.resetTask(TASK_KEY);
    LogStreamRegistry.pushTask(TASK_KEY, "line");
    LogStreamRegistry.completeTask(TASK_KEY);

    // After complete, asFlux should return only buffer (no live sink)
    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY).collectList().block();
    assertNotNull(collected, "Stream result should not be null");
    assertTrue(collected.contains("line"), "Buffer should remain after completeTask");
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

    List<String> collected = LogStreamRegistry.asFlux(TASK_KEY).collectList().block();
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

    await().atMost(5, TimeUnit.SECONDS).until(() -> !completed.isEmpty());
    assertFalse(completed.isEmpty(), "resetTask should complete the existing sink");
  }

  @Test
  void testResetTaskIsIdempotent() {
    assertDoesNotThrow(() -> {
      LogStreamRegistry.resetTask(TASK_KEY);
      LogStreamRegistry.resetTask(TASK_KEY);
    }, "Calling resetTask twice should not throw any exception");
  }
}
