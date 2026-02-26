package com.axonivy.market.controller;

import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.LOGS;

@RestController
@RequiredArgsConstructor
@RequestMapping(LOGS)
@Log4j2
@Tag(name = "Log Viewer API", description = "API to list and view compressed log files")
public class LogController {

  private static final Logger logger = LoggerFactory.getLogger(LogController.class);
  private final LogService logService;

  @GetMapping
  public ResponseEntity<List<LogFileModel>> listGzLogs(
      @Parameter(description = "Filter logs by date (format: yyyy-MM-dd)")
      @RequestParam(required = false) LocalDate date) {
    return ResponseEntity.ok(logService.listGzLogNamesByDate(String.valueOf(date)));
  }

  @GetMapping("/download")
//  @Operation(hidden = true)
  public ResponseEntity<StreamingResponseBody> downloadLog(@RequestParam String fileName) {
    if (!logService.isLogFileExisted(fileName)) {
      return ResponseEntity.notFound().build();
    }

    StreamingResponseBody streamingBody = outputStream -> logService.streamLogContent(fileName, outputStream);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName))
        .body(streamingBody);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> stream() {
    return LogStreamRegistry.asFlux();
  }

  @PostMapping("/log-input")
  public ResponseEntity<Map<String, String>> logUserInput(@RequestBody Map<String, String> input) {
    String message = input.getOrDefault("message", "");
    logger.info("User input: {}", message);
    log.error("Hello Guy");
    return ResponseEntity.ok(Map.of("status", "logged", "message", message));
  }
}
