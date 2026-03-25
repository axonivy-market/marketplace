package com.axonivy.market.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogStreamRegistry {
  private static final int BACKPRESSURE_BUFFER_SIZE = 256;
  private static Sinks.Many<String> sink = Sinks.many().multicast()
      .onBackpressureBuffer(BACKPRESSURE_BUFFER_SIZE, false);
  private static final Map<String, Sinks.Many<String>> taskSinks = new ConcurrentHashMap<>();
  private static final Map<String, List<String>> taskBuffers = new ConcurrentHashMap<>();

  public static Flux<String> asFlux() {
    return sink.asFlux();
  }

  public static boolean hasSubscribers() {
    return sink.currentSubscriberCount() > 0;
  }

  public static void push(String logLine) {
    Sinks.EmitResult result = sink.tryEmitNext(logLine);
    if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
      log.warn("Failed to emit log line to stream registry. Result: {}, Subscribers: {}",
          result, sink.currentSubscriberCount());
    }
  }

  public static Flux<String> asFlux(String taskKey) {
    List<String> buffered = taskBuffers.getOrDefault(taskKey, List.of());
    Sinks.Many<String> taskSink = taskSinks.get(taskKey);

    if (taskSink == null) {
      return Flux.fromIterable(new ArrayList<>(buffered));
    }

    Flux<String> bufferFlux = Flux.fromIterable(new ArrayList<>(buffered));
    Flux<String> liveFlux = taskSink.asFlux();
    return Flux.concat(bufferFlux, liveFlux);
  }

  public static void pushTask(String taskKey, String logLine) {
    taskBuffers.computeIfAbsent(taskKey, k -> new CopyOnWriteArrayList<>()).add(logLine);
    Sinks.Many<String> taskSink = getOrCreateSink(taskKey);

    if (taskSink != null) {
      Sinks.EmitResult result = taskSink.tryEmitNext(logLine);
      if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
        log.warn("Failed to emit log for task: {}. Result: {}", taskKey, result);
      }
    }
  }

  public static void completeTask(String taskKey) {
    // Complete sink → Angular biết stream kết thúc
    Sinks.Many<String> taskSink = taskSinks.remove(taskKey);
    if (taskSink != null) {
      taskSink.tryEmitComplete();
    }
  }

  public static void resetTask(String taskKey) {
    taskBuffers.remove(taskKey);
    completeTask(taskKey);
  }

  private static Sinks.Many<String> getOrCreateSink(String taskKey) {
    return taskSinks.computeIfAbsent(taskKey, k ->
        Sinks.many().multicast().onBackpressureBuffer(BACKPRESSURE_BUFFER_SIZE, false)
    );
  }
}
