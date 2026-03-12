package com.axonivy.market.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseLogAppenderTest {

  private SSELogAppender appender;
  
  @Mock
  private Encoder<ILoggingEvent> mockEncoder;
  
  @Mock
  private ILoggingEvent mockEvent;

  @BeforeEach
  void setUp() {
    appender = new SSELogAppender();
  }

  @Test
  void testStartWithoutEncoder() {
    // Start appender without setting encoder
    appender.start();
    assertTrue(appender.isStarted(), "Appender should be started");
  }

  @Test
  void testStartWithEncoder() {
    appender.setEncoder(mockEncoder);
    appender.start();
    assertTrue(appender.isStarted(), "Appender should be started with encoder");
    verify(mockEncoder, times(1)).start();
  }

  @Test
  void testSetEncoder() {
    appender.setEncoder(mockEncoder);
    assertEquals(mockEncoder, appender.getEncoder(), "Encoder should be set correctly");
  }

  @Test
  void testGetEncoder() {
    appender.setEncoder(mockEncoder);
    Encoder<ILoggingEvent> retrieved = appender.getEncoder();
    assertNotNull(retrieved, "Retrieved encoder should not be null");
    assertEquals(mockEncoder, retrieved, "Retrieved encoder should match the set encoder");
  }

  @Test
  void testAppendWithNoSubscribers() {
    String encodedMessage = "Encoded log message\n";
    appender.setEncoder(mockEncoder);
    appender.start();
    when(mockEncoder.encode(mockEvent)).thenReturn(encodedMessage.getBytes());
    try (MockedStatic<LogStreamRegistry> mockedRegistry = mockStatic(LogStreamRegistry.class)) {
      appender.append(mockEvent);
      mockedRegistry.verify(() -> LogStreamRegistry.push(anyString()), times(1));
    }
  }

  @Test
  void testAppendWithSubscribersAndEncoder() {
    String encodedMessage = "Encoded log message\n";
    appender.setEncoder(mockEncoder);
    appender.start();
    when(mockEncoder.encode(mockEvent)).thenReturn(encodedMessage.getBytes());
    try (MockedStatic<LogStreamRegistry> mockedRegistry = mockStatic(LogStreamRegistry.class)) {
      appender.append(mockEvent);
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      mockedRegistry.verify(() -> LogStreamRegistry.push(captor.capture()), times(1));
      String pushed = captor.getValue();
      assertEquals("Encoded log message", pushed, "Pushed message should be trimmed"); // trimmed
    }
  }

  @Test
  void testAppendWithSubscribersAndNoEncoder() {
    String formattedMessage = "Formatted log message";
    appender.start();
    when(mockEvent.getFormattedMessage()).thenReturn(formattedMessage);
    try (MockedStatic<LogStreamRegistry> mockedRegistry = mockStatic(LogStreamRegistry.class)) {
      appender.append(mockEvent);
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      mockedRegistry.verify(() -> LogStreamRegistry.push(captor.capture()), times(1));
      String pushed = captor.getValue();
      assertEquals(formattedMessage, pushed, "Pushed message should match formatted message");
    }
  }

  @Test
  void testAppendTrimsWhitespace() {
    String encodedMessageWithWhitespace = "  Trimmed message  \n";
    appender.setEncoder(mockEncoder);
    appender.start();
    when(mockEncoder.encode(mockEvent)).thenReturn(encodedMessageWithWhitespace.getBytes());
    try (MockedStatic<LogStreamRegistry> mockedRegistry = mockStatic(LogStreamRegistry.class)) {
      appender.append(mockEvent);
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      mockedRegistry.verify(() -> LogStreamRegistry.push(captor.capture()), times(1));
      String pushed = captor.getValue();
      assertEquals("Trimmed message", pushed, "Pushed message should be properly trimmed");
    }
  }

  @Test
  void testAppendMultipleEvents() {
    String message1 = "First message\n";
    String message2 = "Second message\n";
    appender.setEncoder(mockEncoder);
    appender.start();
    when(mockEncoder.encode(mockEvent))
        .thenReturn(message1.getBytes())
        .thenReturn(message2.getBytes());
    try (MockedStatic<LogStreamRegistry> mockedRegistry = mockStatic(LogStreamRegistry.class)) {
      appender.append(mockEvent);
      appender.append(mockEvent);
      mockedRegistry.verify(() -> LogStreamRegistry.push(anyString()), times(2));
    }
  }

  @Test
  void testAppenderAlreadyStarted() {
    appender.setEncoder(mockEncoder);
    appender.start();
    assertTrue(appender.isStarted(), "Appender should be started initially");
    // Starting again should not cause issues
    appender.start();
    assertTrue(appender.isStarted(), "Appender should still be started after second start call");
    verify(mockEncoder, times(2)).start();
  }

  @Test
  void testAppendEmptyMessage() {
    String emptyMessage = "";
    appender.start();
    when(mockEvent.getFormattedMessage()).thenReturn(emptyMessage);
    try (MockedStatic<LogStreamRegistry> mockedRegistry = mockStatic(LogStreamRegistry.class)) {
      appender.append(mockEvent);
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      mockedRegistry.verify(() -> LogStreamRegistry.push(captor.capture()), times(1));
      String pushed = captor.getValue();
      assertEquals(emptyMessage, pushed, "Pushed message should be empty string");
    }
  }
}
