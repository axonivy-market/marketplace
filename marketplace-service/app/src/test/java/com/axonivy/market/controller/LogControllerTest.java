package com.axonivy.market.controller;

import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axonivy.market.constants.HttpHeaderConstants.X_FORWARDED_FOR;
import static com.axonivy.market.constants.HttpHeaderConstants.X_REAL_IP;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogControllerTest {
  private static final String TASK_KEY = "syncProducts";

  private LogController logController;
  
  @Mock
  private LogService logService;

  @Mock
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    logController = new LogController(logService);
  }

  @Test
  void testListGzLogsWithoutDate() {
    LocalDate date = null;
    List<LogFileModel> mockLogs = Arrays.asList(
        new LogFileModel("application.log", 1024L, null),
        new LogFileModel("application.log.gz", 2048L, "2026-02-26")
    );
    
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(mockLogs);
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(date);
    
    assertNotNull(response, "Response should not be null");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertEquals(2, response.getBody().size(), "Response body should contain 2 log files");
    verify(logService, times(1)).listGzLogNamesByDate(anyString());
  }

  @Test
  void testListGzLogsWithDate() {
    LocalDate date = LocalDate.of(2026, 2, 26);
    List<LogFileModel> mockLogs = Collections.singletonList(
        new LogFileModel("application.2026-02-26.log.gz", 2048L, "2026-02-26")
    );
    
    when(logService.listGzLogNamesByDate("2026-02-26")).thenReturn(mockLogs);
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(date);
    
    assertNotNull(response, "Response should not be null");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertEquals(1, response.getBody().size(), "Response body should contain 1 log file");
    verify(logService, times(1)).listGzLogNamesByDate("2026-02-26");
  }

  @Test
  void testListGzLogsReturnsEmptyList() {
    LocalDate date = LocalDate.of(2026, 1, 1);
    
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(date);
    
    assertNotNull(response, "Response should not be null");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertTrue(response.getBody().isEmpty(), "Response body should be empty when no logs match the date");
  }

  @Test
  void testDownloadLogFileExists() {
    String fileName = "application.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertNotNull(response, "Response should not be null");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK for existing file");
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType(), "Content type should be APPLICATION_OCTET_STREAM");
    assertTrue(response.getHeaders().getFirst("Content-Disposition")
        .contains("attachment"), "Content-Disposition should contain attachment");
    assertTrue(response.getHeaders().getFirst("Content-Disposition")
        .contains(fileName), "Content-Disposition should contain the file name");
  }

  @Test
  void testDownloadLogFileNotFound() {
    String fileName = "non-existent.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(false);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertNotNull(response, "Response should not be null");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Response status should be NOT_FOUND when file does not exist");
  }

  @Test
  void testDownloadLogContentDispositionHeader() {
    String fileName = "application.2026-02-26.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
    assertNotNull(contentDisposition, "Content-Disposition header should not be null");
    assertTrue(contentDisposition.startsWith("attachment"), "Content-Disposition should start with 'attachment'");
    assertTrue(contentDisposition.contains("filename=\"" + fileName + "\""), "Content-Disposition should contain the file name");
  }

  @Test
  void testStreamLogsReturnsFlux() {
    Flux<String> flux = Flux.just("Log 1", "Log 2", "Log 3");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(flux);
      
      Flux<ServerSentEvent<String>> result = logController.stream(request);
      
      assertNotNull(result, "Stream result should not be null");
      List<ServerSentEvent<String>> collected = result
          .take(3)
          .collectList()
          .block();
      assertNotNull(collected, "Collected list should not be null");
      assertEquals(3, collected.size(), "Should have 3 events");
      assertEquals("Log 1", collected.get(0).data(), "First event data should match the first emitted log line");
      assertEquals("Log 2", collected.get(1).data(), "Second event data should match the second emitted log line");
      assertEquals("Log 3", collected.get(2).data(), "Third event data should match the third emitted log line");
    }
  }

  @Test
  void testStreamLogsEmitsMultipleEvents() {
    List<String> logLines = Arrays.asList(
        "[INFO] Application started",
        "[DEBUG] Processing request",
        "[WARN] Warning occurred"
    );
    Flux<String> flux = Flux.fromIterable(logLines);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(flux);
      
      Flux<ServerSentEvent<String>> result = logController.stream(request);
      
      assertNotNull(result, "Stream result should not be null");
      List<ServerSentEvent<String>> collected = result
          .take(logLines.size())
          .collectList()
          .block();
      assertEquals(logLines.size(), collected.size(), "Collected list should contain all expected log lines");
      
      for (int i = 0; i < logLines.size(); i++) {
        assertEquals(logLines.get(i), collected.get(i).data(), "Event data should match log line");
      }
    }
  }

  @Test
  void testStreamLogsReturnsEmptyFlux() {
    Flux<String> emptyFlux = Flux.empty();
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(emptyFlux);
      
      Flux<ServerSentEvent<String>> result = logController.stream(request);
      
      assertNotNull(result, "Stream result should not be null");
      mock.verify(LogStreamRegistry::asFlux, times(1));
    }
  }

  @Test
  void testDownloadLogMultipleFiles() {
    String[] fileNames = {"app1.log", "app2.log", "app3.log"};
    
    for (String fileName : fileNames) {
      when(logService.isLogFileExisted(fileName)).thenReturn(true);
      ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
      
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK for file: " + fileName);
    }
    
    verify(logService, times(3)).isLogFileExisted(anyString());
  }

  @Test
  void testStreamCallsLogStreamRegistry() {
    Flux<String> expectedFlux = Flux.just("test");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(expectedFlux);
      
      Flux<ServerSentEvent<String>> result = logController.stream(request);
      
      assertNotNull(result, "Stream result should not be null");
      mock.verify(LogStreamRegistry::asFlux, times(1));
    }
  }

  @Test
  void testListGzLogsNullDateConversion() {
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(null);
    
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    verify(logService, times(1)).listGzLogNamesByDate("null");
  }

  @Test
  void testDownloadLogWithGzExtension() {
    String fileName = "application.2026-02-26.log.gz";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK for .gz file");
    String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
    assertTrue(contentDisposition.contains(fileName), "Content-Disposition should contain the .gz file name");
  }

  @Test
  void testStreamBodyNotNull() {
    String fileName = "application.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertNotNull(response.getBody(), "Response body should not be null when streaming log file");
  }

  @Test
  void testListGzLogsEmptyList() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(date);
    
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertTrue(response.getBody().isEmpty(), "Response body should be empty for past date with no logs");
  }

  @Test
  void testDownloadLogStreamingResponseBodyCallsService() {
    String fileName = "test.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    verify(logService, times(1)).isLogFileExisted(fileName);
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK after service call");
  }

  @Test
  void testStreamUsesFirstIpFromXForwardedFor() {
    Flux<String> expectedFlux = Flux.just("test");
    when(request.getHeader(X_FORWARDED_FOR)).thenReturn("10.0.0.1, 10.0.0.2");

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(expectedFlux);

      Flux<ServerSentEvent<String>> result = logController.stream(request);

      assertNotNull(result, "Stream result should not be null");
      verify(request, times(1)).getHeader(X_FORWARDED_FOR);
      verify(request, never()).getHeader(X_REAL_IP);
      verify(request, never()).getRemoteAddr();
    }
  }

  @Test
  void testStreamUsesXRealIpWhenForwardedForIsBlank() {
    Flux<String> expectedFlux = Flux.just("test");
    when(request.getHeader(X_FORWARDED_FOR)).thenReturn("   ");
    when(request.getHeader(X_REAL_IP)).thenReturn("192.168.1.10");

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(expectedFlux);

      Flux<ServerSentEvent<String>> result = logController.stream(request);

      assertNotNull(result, "Stream result should not be null");
      verify(request, times(1)).getHeader(X_FORWARDED_FOR);
      verify(request, times(1)).getHeader(X_REAL_IP);
      verify(request, never()).getRemoteAddr();
    }
  }

  @Test
  void testStreamUsesRemoteAddrWhenProxyHeadersMissing() {
    Flux<String> expectedFlux = Flux.just("test");
    when(request.getHeader(X_FORWARDED_FOR)).thenReturn(null);
    when(request.getHeader(X_REAL_IP)).thenReturn(" ");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(expectedFlux);

      Flux<ServerSentEvent<String>> result = logController.stream(request);

      assertNotNull(result, "Stream result should not be null");
      verify(request, times(1)).getHeader(X_FORWARDED_FOR);
      verify(request, times(1)).getHeader(X_REAL_IP);
      verify(request, times(1)).getRemoteAddr();
    }
  }

  @Test
  void testStreamLogsByTaskKeyReturnsFlux() {
    Flux<String> flux = Flux.just("Log 1", "Log 2", "Log 3");

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(flux);

      Flux<String> result = logController.streamLogsByTaskKey(TASK_KEY);

      assertNotNull(result, "Stream result should not be null");
      List<String> collected = result.collectList().block();
      assertNotNull(collected, "Collected list should not be null");
      assertEquals(3, collected.size(), "Should have 3 log lines");
      assertEquals("Log 1", collected.get(0), "First log line should be 'Log 1'");
      assertEquals("Log 2", collected.get(1), "Second log line should be 'Log 2'");
      assertEquals("Log 3", collected.get(2), "Third log line should be 'Log 3'");
    }
  }

  @Test
  void testStreamLogsByTaskKeyEmitsAllEvents() {
    List<String> logLines = Arrays.asList(
        "2026-03-25 10:00:00 INFO c.a.m.ProductService - Starting sync",
        "2026-03-25 10:00:01 WARN c.a.m.ProductService - Warning occurred",
        "2026-03-25 10:00:02 ERROR c.a.m.ProductService - Error occurred"
    );
    Flux<String> flux = Flux.fromIterable(logLines);

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(flux);

      Flux<String> result = logController.streamLogsByTaskKey(TASK_KEY);

      List<String> collected = result.collectList().block();
      assertEquals(
          logLines.size(),
          collected.size(),
          "Collected log size should match the expected number of log lines"
      );
      for (int i = 0; i < logLines.size(); i++) {
        assertEquals(logLines.get(i), collected.get(i), "Log line at index " + i + " should match expected value");
      }
    }
  }

  @Test
  void testStreamLogsByTaskKeyReturnsEmptyFlux() {
    Flux<String> emptyFlux = Flux.empty();

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(emptyFlux);

      Flux<String> result = logController.streamLogsByTaskKey(TASK_KEY);

      assertNotNull(result, "Stream result should not be null");
      List<String> collected = result.collectList().block();
      assertNotNull(collected, "Stream result should not be null");
      assertTrue(collected.isEmpty(), "Should return empty list when no logs");
    }
  }

  @Test
  void testStreamLogsByTaskKeyCallsRegistryWithCorrectKey() {
    Flux<String> flux = Flux.just("test log");

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(flux);

      logController.streamLogsByTaskKey(TASK_KEY);

      mock.verify(() -> LogStreamRegistry.asFlux(TASK_KEY), times(1));
      // Verify other task keys are NOT called
      mock.verify(() -> LogStreamRegistry.asFlux("syncLatestReleasesForProducts"), never());
    }
  }

  @Test
  void testStreamLogsByTaskKeyWithDifferentTaskKeys() {
    List<String> taskKeys = Arrays.asList(
        "syncProducts",
        "syncOneProduct",
        "syncLatestReleasesForProducts",
        "syncGithubMonitor"
    );

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      taskKeys.forEach(key ->
          mock.when(() -> LogStreamRegistry.asFlux(key))
              .thenReturn(Flux.just("log for " + key))
      );

      taskKeys.forEach(key -> {
        Flux<String> result = logController.streamLogsByTaskKey(key);
        assertNotNull(result, "Result should not be null for key: " + key);
        List<String> collected = result.collectList().block();
        assertNotNull(collected, "Stream result should not be null");
        assertEquals(
            1,
            collected.size(),
            "Collected log size should be 1 for task key: " + key
        );
        assertEquals(
            "log for " + key,
            collected.getFirst(),
            "Log content should match expected value for task key: " + key
        );
      });
    }
  }

  @Test
  void testStreamLogsByTaskKeyHandlesClientDisconnect() {
    Flux<String> flux = Flux.just("line 1", "line 2").concatWith(Flux.never());

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(flux);

      Flux<String> result = logController.streamLogsByTaskKey(TASK_KEY);

      assertNotNull(result, "Stream result should not be null");
      // Simulate client cancel
      List<String> collected = result.take(2).collectList().block();
      assertNotNull(collected,"Stream result should not be null");
      assertEquals(2, collected.size(), "Should collect 2 lines before cancel");
    }
  }

  @Test
  void testStreamLogsByTaskKeyHandlesStreamError() {
    RuntimeException error = new RuntimeException("Stream error");
    Flux<String> flux = Flux.concat(
        Flux.just("line before error"),
        Flux.error(error)
    );

    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(() -> LogStreamRegistry.asFlux(TASK_KEY)).thenReturn(flux);

      Flux<String> result = logController.streamLogsByTaskKey(TASK_KEY);

      assertNotNull(result);

      Mono<List<String>> collectedFlux = result.collectList();

      assertThrows(
          RuntimeException.class,
          collectedFlux::block,
          "Should throw RuntimeException when stream emits error"
      );
    }
  }
}
