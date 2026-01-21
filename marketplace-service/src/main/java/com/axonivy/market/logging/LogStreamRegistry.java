package com.axonivy.market.logging;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogStreamRegistry {
  private static final List<SseEmitter> EMITTERS = new CopyOnWriteArrayList<>();

  private LogStreamRegistry() {}

  public static void register(SseEmitter emitter) {
    EMITTERS.add(emitter);
  }

  public static void remove(SseEmitter emitter) {
    EMITTERS.remove(emitter);
  }

  public static boolean hasSubscribers() {
    return !EMITTERS.isEmpty();
  }

  public static void push(String logLine) {
    for (SseEmitter emitter : EMITTERS) {
      try {
        emitter.send(logLine);
      } catch (Exception ex) {
        EMITTERS.remove(emitter);
      }
    }
  }
}
