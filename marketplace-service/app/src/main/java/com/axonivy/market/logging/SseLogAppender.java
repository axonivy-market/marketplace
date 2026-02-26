package com.axonivy.market.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

public class SseLogAppender extends AppenderBase<ILoggingEvent> {
  private Encoder<ILoggingEvent> encoder;

  @Override
  public void start() {
    if (encoder == null) {
      addWarn("No encoder configured for SSE_APPENDER. Initializing default encoder.");
      // encoder = createDefaultEncoder();
    }
    if (encoder != null) {
      encoder.start();
    }
    super.start();
  }

  // private Encoder<ILoggingEvent> createDefaultEncoder() {
  //   PatternLayoutEncoder defaultEncoder = new PatternLayoutEncoder();
  //   defaultEncoder.setContext(getContext());
  //   defaultEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n");
  //   return defaultEncoder;
  // }

  @Override
  protected void append(ILoggingEvent event) {
    if (LogStreamRegistry.hasSubscribers()) {
      String record;
      if (encoder != null) {
        record = new String(encoder.encode(event)).trim();
      } else {
        record = event.getFormattedMessage();
      }
      LogStreamRegistry.push(record);
    }
  }

  public Encoder<ILoggingEvent> getEncoder() {
    return encoder;
  }

  public void setEncoder(Encoder<ILoggingEvent> encoder) {
    this.encoder = encoder;
  }
}
