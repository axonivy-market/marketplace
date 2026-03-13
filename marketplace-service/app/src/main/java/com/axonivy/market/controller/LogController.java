package com.axonivy.market.controller;

import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.logging.LogStreamRegistry;
import com.axonivy.market.model.LogFileModel;
import com.axonivy.market.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

import static com.axonivy.market.constants.HttpHeaderConstants.X_FORWARDED_FOR;
import static com.axonivy.market.constants.HttpHeaderConstants.X_REAL_IP;
import static com.axonivy.market.constants.RequestMappingConstants.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping(LOGS)
@Tag(name = "Log Viewer API", description = "API to list and view compressed log files")
public class LogController {
  private final LogService logService;

  @Authorized
  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<List<LogFileModel>> listGzLogs(
      @Parameter(description = "Filter logs by date (format: yyyy-MM-dd)")
      @RequestParam(required = false) LocalDate date) {
    return ResponseEntity.ok(logService.listGzLogNamesByDate(String.valueOf(date)));
  }

  @Authorized
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

  @Authorized
  @GetMapping(value = LOG_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(hidden = true)
  public Flux<String> stream(HttpServletRequest request) {
    String requesterIp = resolveRequesterIp(request);
    return LogStreamRegistry.asFlux()
        .doOnSubscribe(subscription -> log.info("Log stream client connected from IP: {}", requesterIp))
        .doOnCancel(() -> log.info("Log stream client disconnected from IP: {}", requesterIp))
        .onErrorResume((Throwable error) -> {
          log.error("Error in log stream for IP {}: {}", requesterIp, error.getMessage(), error);
          return Flux.empty();
        });
  }

  private String resolveRequesterIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader(X_FORWARDED_FOR);
    if (StringUtils.isNotBlank(forwardedFor)) {
      return forwardedFor.split(CoreCommonConstants.COMMA)[0].trim();
    }
    String realIp = request.getHeader(X_REAL_IP);
    if (StringUtils.isNotBlank(realIp)) {
      return realIp;
    }
    return request.getRemoteAddr();
  }
}
