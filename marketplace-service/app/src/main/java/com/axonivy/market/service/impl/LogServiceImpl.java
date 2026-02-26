package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import com.axonivy.market.util.FileUtils;
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
public class LogServiceImpl implements LogService {

  @Value("${logging.file.path}")
  private String logPath;

  private static final long CACHE_TTL_MILLIS = 30 * 60 * 1000; // 30 min
  private List<LogFileModel> cachedLogFiles;
  private long lastCacheTime;


  @Override
  public List<LogFileModel> listGzLogNamesByDate(String date) {
    List<LogFileModel> allLogs = getCachedLogFiles();
    
    // If no date provided, return only .log files (not compressed)
    String DEFAULT_DATE_VALUE = "null";
    if (StringUtils.isEmpty(date) || StringUtils.equals(DEFAULT_DATE_VALUE, date)) {
      log.debug("No date provided, returning uncompressed .log files");
      return allLogs.stream()
          .filter(log -> log.getFileName().endsWith(LOG_EXTENSION))
          .collect(Collectors.toList());
    }
    
    // If date provided, filter by that date
    return allLogs.stream()
        .filter(log -> date.equals(log.getDate()))
        .collect(Collectors.toList());
  }

  private List<LogFileModel> getCachedLogFiles() {
    // Check if cache is still valid
    if (cachedLogFiles != null && isCacheValid()) {
      log.debug("Using cached log files");
      return cachedLogFiles;
    }

    // Cache expired or doesn't exist, refresh it
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

    Path path = Paths.get(logPath);
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      log.warn("Logging directory does not exist: {}", logPath);
      return Collections.emptyList();
    }

    try (Stream<Path> stream = Files.list(path)) {
      return stream
          .filter(p -> isLogFile(p.getFileName().toString()))
          .map(p -> {
            try {
              String fileName = p.getFileName().toString();
              String date = extractDateFromFileName(fileName);
              return new LogFileModel(fileName, Files.size(p), date);
            } catch (IOException e) {
              log.error("Failed to get size of log file: {}", p.getFileName(), e);
              return new LogFileModel(p.getFileName().toString(), 0L, null);
            }
          })
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("Failed to list log files in: {}", logPath, e);
      return Collections.emptyList();
    }
  }

  private boolean isLogFile(String fileName) {
    // Accept both .log and .log.gz files
    return fileName.endsWith(LOG_EXTENSION) || fileName.endsWith(CommonConstants.GZ_EXTENSION);
  }

  private String extractDateFromFileName(String fileName) {
    // Pattern: application.yyyy-MM-dd.log or application.yyyy-MM-dd.log.gz
    // or application.yyyy-MM-dd.1.log.gz, etc.
    // Extract the date part (yyyy-MM-dd)
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    final String DATE_MONTH_YEAR_SEPARATOR_REGEX = "\\.";
    final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";

    String[] parts = fileName.split(DATE_MONTH_YEAR_SEPARATOR_REGEX);
    if (parts.length >= 2) {
      // The date is typically the second part: application.[DATE].log(.gz)
      String datePart = parts[1];
      // Validate it looks like a date (yyyy-MM-dd format: 10 characters)
      if (datePart.length() == 10 && datePart.matches(DATE_PATTERN)) {
        return datePart;
      }
    }
    return null;
  }

  @Override
  public void streamLogContent(String fileName, OutputStream outputStream) {
    Path filePath = getLogFilePath(fileName);
    try {
      Files.copy(filePath, outputStream);
    } catch (IOException e) {
      log.error("Failed to stream log file: {}", fileName, e);
    }
  }

  @Override
  public boolean isLogFileExisted(String fileName) {
    Path filePath = getLogFilePath(fileName);
    return Files.exists(filePath) && !Files.isDirectory(filePath);
  }

  private Path getLogFilePath(String fileName) {
    return FileUtils.resolveSafePath(Paths.get(logPath), fileName);
  }
}
