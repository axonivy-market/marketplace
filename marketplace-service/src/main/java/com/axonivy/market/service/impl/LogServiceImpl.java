package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.service.LogService;
import com.axonivy.market.util.FileUtils;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Service
public class LogServiceImpl implements LogService {

  @Value("${logging.file.path}")
  private String logPath;

  @Override
  public List<String> listGzLogNames() {
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
          .filter(p -> p.toString().endsWith(CommonConstants.GZ_EXTENSION))
          .map(Path::getFileName).map(Path::toString)
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("Failed to list log files in: {}", logPath, e);
      return Collections.emptyList();
    }
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
