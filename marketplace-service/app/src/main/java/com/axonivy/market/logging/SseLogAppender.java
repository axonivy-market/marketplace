package com.axonivy.market.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

public class SseLogAppender extends AppenderBase<ILoggingEvent> {
  private Encoder<ILoggingEvent> encoder;

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
