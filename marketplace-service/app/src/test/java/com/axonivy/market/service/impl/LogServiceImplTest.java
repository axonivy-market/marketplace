package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

  private LogServiceImpl logService;
  
  @TempDir
  private Path tempDir;

  @BeforeEach
  void setUp() {
    logService = new LogServiceImpl(null, new ArrayList<>(), 0);
  }

  @Test
  void testListGzLogNamesByDateWithNullDate() throws IOException {
    // Create test log files
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate(null);
    
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.size(), "Should contain 1 log file");
    assertTrue(result.get(0).getFileName().endsWith(".log"), "File should have .log extension");
  }

  @Test
  void testListGzLogNamesByDateWithNullStringDate() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.size(), "Should contain 1 log file");
    assertTrue(result.get(0).getFileName().endsWith(".log"), "File should have .log extension");
  }

  @Test
  void testListGzLogNamesByDateWithEmptyString() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("");
    
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.size(), "Should contain 1 log file");
    assertTrue(result.get(0).getFileName().endsWith(".log"), "File should have .log extension");
  }

  @Test
  void testListGzLogNamesByDateWithSpecificDate() throws IOException {
    Path logFile1 = tempDir.resolve("application.2026-02-20.log");
    Path logFile2 = tempDir.resolve("application.2026-02-26.log");
    Path logFile3 = tempDir.resolve("application.2026-02-26.log.gz");
    
    Files.createFile(logFile1);
    Files.createFile(logFile2);
    Files.createFile(logFile3);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("2026-02-26");
    
    assertNotNull(result, "Result should not be null");
    assertEquals(2, result.size(), "Should contain 2 log files for the specified date");
    assertTrue(result.stream().allMatch(log -> log.getDate().equals("2026-02-26")), "All results should have the specified date");
  }

  @Test
  void testListGzLogNamesByDateFiltersByLogExtension() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Path gzFile = tempDir.resolve("application.log.gz");
    Path txtFile = tempDir.resolve("application.txt");
    
    Files.createFile(logFile);
    Files.createFile(gzFile);
    Files.createFile(txtFile);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.size(), "Should contain 1 .log file");
    assertEquals("application.log", result.get(0).getFileName(), "Should filter only .log files");
  }

  @Test
  void testListGzLogNamesByDateWithNonExistentPath() {
    logService = createLogServiceWithPath("/non/existent/path");
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Result should be empty for non-existent path");
  }

  @Test
  void testListGzLogNamesByDateWithEmptyLogPath() {
    logService = createLogServiceWithPath("");
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Result should be empty for empty log path");
  }

  @Test
  void testListGzLogNamesByDateWithNullLogPath() {
    logService = createLogServiceWithPath(null);
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Result should be empty for null log path");
  }

  @Test
  void testCachingOfLogFiles() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result1 = logService.listGzLogNamesByDate("null");
    List<LogFileModel> result2 = logService.listGzLogNamesByDate("null");
    
    assertEquals(result1.size(), result2.size(), "Cached results should have same size");
    assertEquals(result1.get(0).getFileName(), result2.get(0).getFileName(), "Cached results should have same file names");
  }

  @Test
  void testExtractDateFromFileName() throws IOException {
    logService = createLogServiceWithPath(tempDir.toString());
    
    // Test cases with different date formats
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    // Create specific files and test
    Path logFile = tempDir.resolve("application.2026-02-26.log");
    Files.createFile(logFile);
    
    // Refresh cache
    logService = createLogServiceWithPath(tempDir.toString());
    result = logService.listGzLogNamesByDate("2026-02-26");
    
    assertTrue(result.size() > 0, "Result should not be empty");
    assertTrue(result.stream().anyMatch(log -> "2026-02-26".equals(log.getDate())), "Should extract date from file name");
  }

  @Test
  void testIsLogFileExistedReturnsTrue() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(logFile);
      
      logService = createLogServiceWithPath(tempDir.toString());
      
      boolean exists = logService.isLogFileExisted("application.log");
      
      assertTrue(exists, "Log file should exist");
    }
  }

  @Test
  void testIsLogFileExistedReturnsFalse() throws IOException {
    Path nonExistentFile = tempDir.resolve("non-existent.log");
    
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(nonExistentFile);
      
      logService = createLogServiceWithPath(tempDir.toString());
      
      boolean exists = logService.isLogFileExisted("non-existent.log");
      
      assertFalse(exists, "Non-existent log file should return false");
    }
  }

  @Test
  void testStreamLogContent() throws IOException {
    String content = "Log file content";
    Path logFile = tempDir.resolve("application.log");
    Files.write(logFile, content.getBytes());
    
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(logFile);
      
      logService = createLogServiceWithPath(tempDir.toString());
      
      OutputStream outputStream = new ByteArrayOutputStream();
      logService.streamLogContent("application.log", outputStream);
      
      String result = outputStream.toString();
      assertEquals(content, result, "Streamed content should match original file content");
    }
  }

  @Test
  void testStreamLogContentWithNonExistentFile() throws IOException {
    Path nonExistentFile = tempDir.resolve("non-existent.log");
    
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(nonExistentFile);
      
      logService = createLogServiceWithPath(tempDir.toString());
      
      OutputStream outputStream = new ByteArrayOutputStream();
      assertDoesNotThrow(() -> logService.streamLogContent("non-existent.log", outputStream), "Streaming non-existent file should not throw exception");
    }
  }

  @Test
  void testLogFileModelWithSize() throws IOException {
    String content = "This is a test file";
    Path logFile = tempDir.resolve("application.log");
    Files.write(logFile, content.getBytes());
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertTrue(result.size() > 0, "Result should not be empty");
    assertTrue(result.get(0).getSize() > 0, "Log file model should have size greater than 0");
  }

  @Test
  void testExtractDateFromFileNameWithoutDate() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    
    assertTrue(result.size() > 0, "Result should not be empty");
    assertNull(result.get(0).getDate(), "File name without date should have null date");
  }

  @Test
  void testListMultipleDatesAndFilters() throws IOException {
    Path file1 = tempDir.resolve("application.2026-02-20.log");
    Path file2 = tempDir.resolve("application.2026-02-20.log.gz");
    Path file3 = tempDir.resolve("application.2026-02-26.log");
    Path file4 = tempDir.resolve("application.2026-02-26.log.gz");
    
    Files.createFile(file1);
    Files.createFile(file2);
    Files.createFile(file3);
    Files.createFile(file4);
    
    logService = createLogServiceWithPath(tempDir.toString());
    
    List<LogFileModel> feb20 = logService.listGzLogNamesByDate("2026-02-20");
    List<LogFileModel> feb26 = logService.listGzLogNamesByDate("2026-02-26");
    
    assertEquals(2, feb20.size(), "Should find 2 files for 2026-02-20");
    assertEquals(2, feb26.size(), "Should find 2 files for 2026-02-26");
  }

  private LogServiceImpl createLogServiceWithPath(String path) {
    LogServiceImpl service = new LogServiceImpl(path, new ArrayList<>(), 0);
    return service;
  }
}
