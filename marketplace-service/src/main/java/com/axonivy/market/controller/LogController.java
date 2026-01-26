package com.axonivy.market.controller;

import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.service.LogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.LOGS;

@RestController
@RequiredArgsConstructor
@RequestMapping(LOGS)
@Tag(name = "Log Viewer API", description = "API to list and view compressed log files")
public class LogController {

  private final LogService logService;

  @GetMapping
//  @Operation(hidden = true)
  public ResponseEntity<List<String>> listGzLogs() {
    return ResponseEntity.ok(logService.listGzLogNames());
  }

  @GetMapping("/{fileName}")
//  @Operation(hidden = true)
  public ResponseEntity<StreamingResponseBody> downloadLog(@PathVariable String fileName) {
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
}
