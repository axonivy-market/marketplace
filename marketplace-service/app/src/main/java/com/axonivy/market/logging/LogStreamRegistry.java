package com.axonivy.market.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

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
  private static final int CURRENT_LOG_LINES_LIMIT = 1000;

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
    Sinks.Many<String> taskSink = getOrCreateSink(taskKey);
    return taskSink.asFlux();
  }

  public static void pushTask(String taskKey, String logLine) {
    Sinks.Many<String> taskSink = getOrCreateSink(taskKey);

    if (taskSink != null) {
      Sinks.EmitResult result = taskSink.tryEmitNext(logLine);

      if (result == Sinks.EmitResult.FAIL_TERMINATED) {
        taskSinks.remove(taskKey);
        pushTask(taskKey, logLine);
        return;
      }

      if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
        log.warn("Failed to emit log for task: {}. Result: {}", taskKey, result);
      }
    }
  }

  public static void completeTask(String taskKey) {
    Sinks.Many<String> taskSink = taskSinks.remove(taskKey);
    if (taskSink != null) {
      taskSink.tryEmitComplete();
    }
  }

  public static void resetTask(String taskKey) {
    completeTask(taskKey);
  }

  private static Sinks.Many<String> getOrCreateSink(String taskKey) {
    return taskSinks.computeIfAbsent(taskKey, k ->
        Sinks.many().replay().limit(CURRENT_LOG_LINES_LIMIT)
    );
  }
}
