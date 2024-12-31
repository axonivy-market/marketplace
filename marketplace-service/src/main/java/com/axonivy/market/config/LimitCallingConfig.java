package com.axonivy.market.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class LimitCallingConfig extends OncePerRequestFilter {
  private static final String REQUEST_HEADER = "X-Forwarded-For";
  @Value("${market.allowed.click-capacity}")
  private int capacity;

  @Value("${market.limited.request-paths}")
  private List<String> requestPaths;
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = getClientIp(request);
    String apiPath = request.getRequestURI();
    log.warn("apiPath: {}", apiPath);
    log.warn("requestPaths: {}", requestPaths.toString());
    if (requestPaths.stream().anyMatch(path -> apiPath.matches(path + ".*"))) {
      Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

      if (bucket.tryConsume(1)) {
        log.warn("Request allowed for IP: {}. Remaining tokens: {}", clientIp, bucket.getAvailableTokens());
        filterChain.doFilter(request, response);
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        response.getWriter().write("Too many requests. Please try again later.");
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private Bucket createNewBucket(String clientIp) {
    Bandwidth limit = Bandwidth.builder()
        .capacity(capacity)
        .refillGreedy(capacity, Duration.ofMinutes(1))
        .build();
    return Bucket.builder().addLimit(limit).build();
  }

  private String getClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader(REQUEST_HEADER);
    if (StringUtils.isNotEmpty(forwardedFor)) {
      return forwardedFor.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}