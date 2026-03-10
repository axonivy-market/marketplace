package com.axonivy.market.controller;

import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(LOGS)
@Tag(name = "Log Viewer API", description = "API to list and view compressed log files")
public class LogController {
  private final LogService logService;

  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<List<LogFileModel>> listGzLogs(
      @Parameter(description = "Filter logs by date (format: yyyy-MM-dd)")
      @RequestParam(required = false) LocalDate date) {
    return ResponseEntity.ok(logService.listGzLogNamesByDate(String.valueOf(date)));
  }

  @GetMapping(DOWNLOAD_LOG_ARTIFACT)
  @Operation(hidden = true)
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

  @GetMapping(value = LOG_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(hidden = true)
  public Flux<ServerSentEvent<String>> stream() {
    Flux<ServerSentEvent<String>> logEvents = LogStreamRegistry.asFlux()
        .map(logLine -> ServerSentEvent.builder(logLine).build());
    Flux<ServerSentEvent<String>> heartbeats = Flux.interval(Duration.ofSeconds(20))
        .map(tick -> ServerSentEvent.<String>builder().comment("keep-alive").build());
    return Flux.merge(logEvents, heartbeats);
  }
}
