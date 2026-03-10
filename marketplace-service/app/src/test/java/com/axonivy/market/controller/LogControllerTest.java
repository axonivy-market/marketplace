package com.axonivy.market.controller;

import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogControllerTest {

  private LogController logController;
  
  @Mock
  private LogService logService;

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
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(flux);
      
      Flux<ServerSentEvent<String>> result = logController.stream();
      
      assertNotNull(result, "Stream result should not be null");
      assertNotNull(result, "Stream result should not be null");
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
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(flux);
      
      Flux<ServerSentEvent<String>> result = logController.stream();
      
      assertNotNull(result, "Stream result should not be null");
      List<String> collected = result
          .flatMap(event -> event.data() == null ? Flux.empty() : Flux.just(event.data()))
          .take(logLines.size())
          .collectList()
          .block();
      assertEquals(3, collected.size(), "Collected list should contain 3 log lines");
      assertTrue(collected.containsAll(logLines), "Collected list should contain all expected log lines");
    }
  }

  @Test
  void testStreamLogsReturnsEmptyFlux() {
    Flux<String> emptyFlux = Flux.empty();
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(emptyFlux);
      
      Flux<ServerSentEvent<String>> result = logController.stream();
      
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
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(expectedFlux);
      
      Flux<ServerSentEvent<String>> result = logController.stream();
      
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
}
