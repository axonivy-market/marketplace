package com.axonivy.market.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogStreamRegistry {
  private static Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer(256, false);

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
}
