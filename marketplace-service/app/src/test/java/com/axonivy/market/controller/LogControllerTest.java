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
    
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
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
    
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    verify(logService, times(1)).listGzLogNamesByDate("2026-02-26");
  }

  @Test
  void testListGzLogsReturnsEmptyList() {
    LocalDate date = LocalDate.of(2026, 1, 1);
    
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(date);
    
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void testDownloadLogFileExists() {
    String fileName = "application.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    assertTrue(response.getHeaders().getFirst("Content-Disposition")
        .contains("attachment"));
    assertTrue(response.getHeaders().getFirst("Content-Disposition")
        .contains(fileName));
  }

  @Test
  void testDownloadLogFileNotFound() {
    String fileName = "non-existent.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(false);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testDownloadLogContentDispositionHeader() {
    String fileName = "application.2026-02-26.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
    assertNotNull(contentDisposition);
    assertTrue(contentDisposition.startsWith("attachment"));
    assertTrue(contentDisposition.contains("filename=\"" + fileName + "\""));
  }

  @Test
  void testStreamLogsReturnsFlux() {
    Flux<String> flux = Flux.just("Log 1", "Log 2", "Log 3");
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(flux);
      
      Flux<String> result = logController.stream();
      
      assertNotNull(result);
      assertEquals(flux, result);
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
      
      Flux<String> result = logController.stream();
      
      assertNotNull(result);
      List<String> collected = result.collectList().block();
      assertEquals(3, collected.size());
      assertTrue(collected.containsAll(logLines));
    }
  }

  @Test
  void testStreamLogsReturnsEmptyFlux() {
    Flux<String> emptyFlux = Flux.empty();
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(emptyFlux);
      
      Flux<String> result = logController.stream();
      
      assertNotNull(result);
      List<String> collected = result.collectList().block();
      assertTrue(collected.isEmpty());
    }
  }

  @Test
  void testDownloadLogMultipleFiles() {
    String[] fileNames = {"app1.log", "app2.log", "app3.log"};
    
    for (String fileName : fileNames) {
      when(logService.isLogFileExisted(fileName)).thenReturn(true);
      ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
      
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    verify(logService, times(3)).isLogFileExisted(anyString());
  }

  @Test
  void testStreamCallsLogStreamRegistry() {
    Flux<String> expectedFlux = Flux.just("test");
    
    try (MockedStatic<LogStreamRegistry> mock = mockStatic(LogStreamRegistry.class)) {
      mock.when(LogStreamRegistry::asFlux).thenReturn(expectedFlux);
      
      Flux<String> result = logController.stream();
      
      assertNotNull(result);
      mock.verify(LogStreamRegistry::asFlux, times(1));
    }
  }

  @Test
  void testListGzLogsNullDateConversion() {
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(null);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(logService, times(1)).listGzLogNamesByDate("null");
  }

  @Test
  void testDownloadLogWithGzExtension() {
    String fileName = "application.2026-02-26.log.gz";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
    assertTrue(contentDisposition.contains(fileName));
  }

  @Test
  void testStreamBodyNotNull() {
    String fileName = "application.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    assertNotNull(response.getBody());
  }

  @Test
  void testListGzLogsEmptyList() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    
    when(logService.listGzLogNamesByDate(anyString())).thenReturn(Collections.emptyList());
    
    ResponseEntity<List<LogFileModel>> response = logController.listGzLogs(date);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void testDownloadLogStreamingResponseBodyCallsService() {
    String fileName = "test.log";
    
    when(logService.isLogFileExisted(fileName)).thenReturn(true);
    
    ResponseEntity<StreamingResponseBody> response = logController.downloadLog(fileName);
    
    verify(logService, times(1)).isLogFileExisted(fileName);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
