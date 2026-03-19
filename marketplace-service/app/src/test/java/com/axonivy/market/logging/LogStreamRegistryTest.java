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

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class LogStreamRegistryTest {

  @Test
  void testAsFluxReturnsNonNullFlux() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    assertNotNull(flux, "LogStreamRegistry.asFlux() should return a non-null Flux");
  }

  @Test
  void testHasSubscribersReturnsTrueWhenSubscribed() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    flux.subscribe(value -> {}, error -> {}, () -> {});
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
    await().atMost(5, TimeUnit.SECONDS).until(() -> subscriber1Records.contains(testMessage) && subscriber2Records.contains(testMessage));
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
}
