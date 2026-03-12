package com.axonivy.market.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogStreamRegistry {
  private static final Sinks.Many<String> SINK =
      Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

  public static Flux<String> asFlux() {
    return SINK.asFlux();
  }

  public static boolean hasSubscribers() {
    return SINK.currentSubscriberCount() > 0;
  }

  public static void push(String logLine) {
    Sinks.EmitResult result = SINK.tryEmitNext(logLine);
    if (result.isFailure()) {
      log.warn("Failed to emit log line to stream registry. Result: {}, Subscribers: {}", 
               result, SINK.currentSubscriberCount());
    }
  }
}
