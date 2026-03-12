package com.axonivy.market.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogStreamRegistry {
  private static final Sinks.Many<String> SINK = Sinks.many().replay().limit(1);

  public static Flux<String> asFlux() {
    return SINK.asFlux();
  }

  public static boolean hasSubscribers() {
    return SINK.currentSubscriberCount() > 0;
  }

  public static void push(String logLine) {
    Sinks.EmitResult result = SINK.tryEmitNext(logLine);
    if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
      log.warn("Failed to emit log line to stream registry. Result: {}, Subscribers: {}",
          result, SINK.currentSubscriberCount());
    }
  }
}
