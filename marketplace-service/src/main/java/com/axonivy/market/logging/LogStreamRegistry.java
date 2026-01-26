package com.axonivy.market.logging;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class LogStreamRegistry {
  private static final Sinks.Many<String> SINK = Sinks.many().multicast().onBackpressureBuffer();

  private LogStreamRegistry() {}

  public static Flux<String> asFlux() {
    return SINK.asFlux();
  }

  public static boolean hasSubscribers() {
    return SINK.currentSubscriberCount() > 0;
  }

  public static void push(String logLine) {
    SINK.tryEmitNext(logLine);
  }
}
