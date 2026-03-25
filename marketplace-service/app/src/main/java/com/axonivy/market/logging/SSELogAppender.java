package com.axonivy.market.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import com.axonivy.market.constants.LoggingConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SSELogAppender extends AppenderBase<ILoggingEvent> {
  private Encoder<ILoggingEvent> encoder;

  @Override
  public void start() {
    if (encoder == null) {
      addWarn("No encoder configured for SSE_APPENDER. Initializing default encoder.");
    } else {
      encoder.start();
    }
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    String logLine = resolveLogRecord(event);
    LogStreamRegistry.push(logLine);

    String taskKey = event.getMDCPropertyMap().get(LoggingConstants.TASK_KEY);
    if (taskKey != null) {
      LogStreamRegistry.pushTask(taskKey, logLine);
    }
  }

  private String resolveLogRecord(ILoggingEvent event) {
    if (encoder == null) {
      return formattedMessage(event);
    }

    byte[] encoded = encoder.encode(event);
    if (encoded == null) {
      return formattedMessage(event);
    }
    return new String(encoded).trim();
  }

  private String formattedMessage(ILoggingEvent event) {
    return Objects.toString(event.getFormattedMessage(), "");
  }
}
