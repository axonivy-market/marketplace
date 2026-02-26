package com.axonivy.market.logging;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogStreamRegistryTest {

  @Test
  void testAsFluxReturnsNonNullFlux() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    assertNotNull(flux);
  }

  @Test
  void testHasSubscribersReturnsTrueWhenSubscribed() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    boolean initialSubscriberCount = LogStreamRegistry.hasSubscribers();
    
    flux.subscribe(value -> {}, error -> {}, () -> {});
    
    // After subscribing, should have subscribers
    boolean afterSubscriptionHasSubscribers = LogStreamRegistry.hasSubscribers();
    
    assertTrue(afterSubscriptionHasSubscribers);
  }

  @Test
  void testPushEmitsLogLine() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    String testMessage = "Test log message";
    List<String> received = new ArrayList<>();
    
    flux.subscribe(received::add);
    LogStreamRegistry.push(testMessage);
    
    // Give time for async processing
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    assertTrue(received.contains(testMessage));
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
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    assertTrue(received.contains(message1));
    assertTrue(received.contains(message2));
    assertTrue(received.contains(message3));
  }

  @Test
  void testPushWithEmptyString() {
    Flux<String> flux = LogStreamRegistry.asFlux();
    String emptyMessage = "";
    List<String> received = new ArrayList<>();
    
    flux.subscribe(received::add);
    LogStreamRegistry.push(emptyMessage);
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    assertTrue(received.contains(emptyMessage));
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
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    assertTrue(subscriber1Records.contains(testMessage));
    assertTrue(subscriber2Records.contains(testMessage));
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
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    assertTrue(received1.size() >= 1);
    assertTrue(received2.size() >= 1);
  }
}
