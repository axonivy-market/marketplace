package com.axonivy.market.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SseLogAppender extends AppenderBase<ILoggingEvent> {
  private Encoder<ILoggingEvent> encoder;

  @Override
  public void start() {
    if (encoder == null) {
      addWarn("No encoder configured for SSE_APPENDER. Initializing default encoder.");
    }
    if (encoder != null) {
      encoder.start();
    }
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (LogStreamRegistry.hasSubscribers()) {
      String logsRecord;
      if (encoder != null) {
        logsRecord = new String(encoder.encode(event)).trim();
      } else {
        logsRecord = event.getFormattedMessage();
      }
      LogStreamRegistry.push(logsRecord);
    }
  }
}
