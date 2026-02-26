package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import com.axonivy.market.util.FileUtils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.constants.CommonConstants.LOG_EXTENSION;

@Log4j2
@Service
@NoArgsConstructor
public class LogServiceImpl implements LogService {
  private static final long CACHE_TTL_MILLIS = 30 * 60 * 1000L; // 30 min
  @Value("${logging.file.path}")
  private String logPath;
  private List<LogFileModel> cachedLogFiles;
  private long lastCacheTime;

  @Override
  public List<LogFileModel> listGzLogNamesByDate(String date) {
    List<LogFileModel> allLogs = getCachedLogFiles();
    final String nullValue = "null";
    if (StringUtils.isEmpty(date) || StringUtils.equals(nullValue, date)) {
      log.debug("No date provided, returning uncompressed .log files");
      return allLogs.stream()
          .filter(log -> log.getFileName().endsWith(LOG_EXTENSION))
          .toList();
    }
    return allLogs.stream()
        .filter(log -> date.equals(log.getDate()))
        .toList();
  }

  private List<LogFileModel> getCachedLogFiles() {
    if (cachedLogFiles != null && isCacheValid()) {
      log.debug("Using cached log files");
      return cachedLogFiles;
    }
    log.debug("Refreshing log files cache");
    cachedLogFiles = loadLogFilesFromDisk();
    lastCacheTime = System.currentTimeMillis();
    return cachedLogFiles;
  }

  private boolean isCacheValid() {
    return System.currentTimeMillis() - lastCacheTime < CACHE_TTL_MILLIS;
  }

  private List<LogFileModel> loadLogFilesFromDisk() {
    if (logPath == null || logPath.isEmpty()) {
      log.warn("Logging file path is not configured.");
      return Collections.emptyList();
    }

    var path = Paths.get(logPath);
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      log.warn("Logging directory does not exist: {}", logPath);
      return Collections.emptyList();
    }

    try (Stream<Path> stream = Files.list(path)) {
      return stream
          .filter(filePath -> isLogFile(filePath.getFileName().toString()))
          .map(filePath -> {
            try {
              var fileName = filePath.getFileName().toString();
              String date = extractDateFromFileName(fileName);
              return new LogFileModel(fileName, Files.size(filePath), date);
            } catch (IOException e) {
              log.error("Failed to get size of log file: {}", filePath.getFileName(), e);
              return new LogFileModel(filePath.getFileName().toString(), 0L, null);
            }
          })
          .toList();
    } catch (IOException e) {
      log.error("Failed to list log files in: {}", logPath, e);
      return Collections.emptyList();
    }
  }

  private boolean isLogFile(String fileName) {
    return fileName.endsWith(LOG_EXTENSION) || fileName.endsWith(CommonConstants.GZ_EXTENSION);
  }

  private String extractDateFromFileName(String fileName) {
    // Pattern: application.yyyy-MM-dd.log or application.yyyy-MM-dd.log.gz
    // or application.yyyy-MM-dd.1.log.gz, etc.
    // Extract the date part (yyyy-MM-dd)
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    final String dateMonthYearSeparator = "\\.";
    final String datePattern = "\\d{4}-\\d{2}-\\d{2}";
    final int minExpectedParts = 2;
    final int dateYearCharCount = 10; // yyyy-MM-dd is 10 characters
    String[] parts = fileName.split(dateMonthYearSeparator);
    if (parts.length >= minExpectedParts) {
      // The date is typically the second part: application.[DATE].log(.gz)
      String datePart = parts[1];
      // Validate it looks like a date (yyyy-MM-dd format: 10 characters)
      if (datePart.length() == dateYearCharCount && datePart.matches(datePattern)) {
        return datePart;
      }
    }
    return null;
  }

  @Override
  public void streamLogContent(String fileName, OutputStream outputStream) {
    var filePath = getLogFilePath(fileName);
    try {
      Files.copy(filePath, outputStream);
    } catch (IOException e) {
      log.error("Failed to stream log file: {}", fileName, e);
    }
  }

  @Override
  public boolean isLogFileExisted(String fileName) {
    var filePath = getLogFilePath(fileName);
    return Files.exists(filePath) && !Files.isDirectory(filePath);
  }

  private Path getLogFilePath(String fileName) {
    return FileUtils.resolveSafePath(Paths.get(logPath), fileName);
  }
}
