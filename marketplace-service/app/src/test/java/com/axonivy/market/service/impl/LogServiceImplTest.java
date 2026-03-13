package com.axonivy.market.service.impl;

import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

  @InjectMocks
  private LogServiceImpl logService;
  
  @TempDir
  private Path tempDir;
  
  @BeforeEach
  void setUp() {
    // Set the log path to temp directory using reflection for testing
    ReflectionTestUtils.setField(logService, "logPath", tempDir.toString());
  }

  @Test
  void testListGzLogNamesByDateWithNullDate() throws IOException {
    // Create test log files
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    List<LogFileModel> result = logService.listGzLogNamesByDate(null);
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Should return empty list for null date");
    result = logService.listGzLogNamesByDate("null");
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Should return empty list for 'null' string date");
  }

  @Test
  void testListGzLogNamesByDateWithPastDate() throws IOException {
    Path logFile1 = tempDir.resolve("application.2026-02-20.log");
    Path logFile2 = tempDir.resolve("application.2026-02-26.log");
    Path logFile3 = tempDir.resolve("application.2026-02-26.log.gz");
    Files.createFile(logFile1);
    Files.createFile(logFile2);
    Files.createFile(logFile3);
    List<LogFileModel> result = logService.listGzLogNamesByDate("2026-02-26");
    assertNotNull(result, "Result should not be null");
    assertEquals(2, result.size(), "Should contain 2 log files for the specified date");
    assertTrue(result.stream().allMatch(log -> log.getDate().equals("2026-02-26")), "All results should have the specified date");
  }

  @Test
  void testListGzLogNamesByDateWithEmptyDate() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Path gzFile = tempDir.resolve("application.log.gz");
    Path txtFile = tempDir.resolve("application.txt");
    Files.createFile(logFile);
    Files.createFile(gzFile);
    Files.createFile(txtFile);
    List<LogFileModel> result = logService.listGzLogNamesByDate("");
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Should return empty list for empty date string");
  }

  @Test
  void testListGzLogNamesByDateWithNullLogPath() {
    List<LogFileModel> result = logService.listGzLogNamesByDate("null");
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Result should be empty for null log path");
  }

  @Test
  void testListGzLogNamesByDateWithInvalidDateFormat() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    List<LogFileModel> result = logService.listGzLogNamesByDate("invalid-date");
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Should return empty list for invalid date format");
  }

  @Test
  void testListGzLogNamesByDateWithTodayDate() throws IOException {
    // Create both .log and .gz files for today
    LocalDate today = LocalDate.now();
    String todayString = today.toString(); // Format: yyyy-MM-dd
    Path logFileWithDate = tempDir.resolve("application." + todayString + ".log");
    Path gzFileWithDate = tempDir.resolve("application." + todayString + ".log.gz");
    Path uncompressedFile = tempDir.resolve("application.log");
    Path otherDateLog = tempDir.resolve("application.2026-02-20.log");
    Files.createFile(logFileWithDate);
    Files.createFile(gzFileWithDate);
    Files.createFile(uncompressedFile);
    Files.createFile(otherDateLog);
    List<LogFileModel> result = logService.listGzLogNamesByDate(todayString);
    assertNotNull(result, "Result should not be null");
    assertEquals(4, result.size(), "Should contain all .log files and today's dated files");
    assertTrue(result.stream().anyMatch(log -> ("application." + todayString + ".log").equals(log.getFileName())), 
        "Should include .log with today's date");
    assertTrue(result.stream().anyMatch(log -> "application.log".equals(log.getFileName())), 
        "Should include uncompressed .log");
    assertTrue(result.stream().anyMatch(log -> ("application." + todayString + ".log.gz").equals(log.getFileName())), 
      "Should include .gz with today's date");
    assertTrue(result.stream().anyMatch(log -> "application.2026-02-20.log".equals(log.getFileName())), 
      "Should include .log files from other dates");
  }

  @Test
  void testIsLogFileExistedReturnsTrue() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(logFile);
      boolean exists = logService.isLogFileExisted("application.log");
      assertTrue(exists, "Log file should exist");
    }
  }

  @Test
  void testIsLogFileExistedReturnsFalse() {
    Path nonExistentFile = tempDir.resolve("non-existent.log");
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(nonExistentFile);
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
      OutputStream outputStream = new ByteArrayOutputStream();
      logService.streamLogContent("application.log", outputStream);
      String result = outputStream.toString();
      assertEquals(content, result, "Streamed content should match original file content");
    }
  }

  @Test
  void testStreamLogContentWithNonExistentFile() {
    Path nonExistentFile = tempDir.resolve("non-existent.log");
    try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
      mockedFileUtils.when(() -> FileUtils.resolveSafePath(any(), any()))
          .thenReturn(nonExistentFile);
      OutputStream outputStream = new ByteArrayOutputStream();
      assertDoesNotThrow(() -> logService.streamLogContent("non-existent.log", outputStream), "Streaming non-existent file should not throw exception");
    }
  }

  @Test
  void testLogFileModelWithSize() throws IOException {
    String content = "This is a test file";
    LocalDate today = LocalDate.now();
    String todayString = today.toString();
    Path logFile = tempDir.resolve("application." + todayString + ".log");
    Files.write(logFile, content.getBytes());
    List<LogFileModel> result = logService.listGzLogNamesByDate(todayString);
    assertTrue(result.size() > 0, "Result should not be empty");
    assertTrue(result.get(0).getSize() > 0, "Log file model should have size greater than 0");
  }

  @Test
  void testExtractDateFromFileNameWithoutDate() throws IOException {
    Path logFile = tempDir.resolve("application.log");
    Files.createFile(logFile);
    LocalDate today = LocalDate.now();
    String todayString = today.toString();
    List<LogFileModel> result = logService.listGzLogNamesByDate(todayString);
    assertTrue(result.size() > 0, "Result should not be empty");
    assertNull(result.get(0).getDate(), "File name without date should have null date");
  }

  @Test
  void testCachingOfLogFiles() throws IOException {
    LocalDate today = LocalDate.now();
    String todayString = today.toString();
    Path logFile = tempDir.resolve("application." + todayString + ".log");
    Files.createFile(logFile);
    List<LogFileModel> result1 = logService.listGzLogNamesByDate(todayString);
    List<LogFileModel> result2 = logService.listGzLogNamesByDate(todayString);
    assertEquals(result1.size(), result2.size(), "Cached results should have same size");
    assertEquals(result1.get(0).getFileName(), result2.get(0).getFileName(), "Cached results should have same file names");
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
    List<LogFileModel> feb20 = logService.listGzLogNamesByDate("2026-02-20");
    List<LogFileModel> feb26 = logService.listGzLogNamesByDate("2026-02-26");
    assertEquals(2, feb20.size(), "Should find 2 files for 2026-02-20");
    assertEquals(2, feb26.size(), "Should find 2 files for 2026-02-26");
  }
}
