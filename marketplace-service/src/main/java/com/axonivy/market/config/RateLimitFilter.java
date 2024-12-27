package com.axonivy.market.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private static final String REQUEST_PATH = "/api";
  @Value("${market.allowed.download-capacity}")
  private int capacity;
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = getClientIp(request);
    String apiPath = request.getRequestURI();

    if (apiPath.contains(REQUEST_PATH)) {
      Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

      if (bucket.tryConsume(1)) {
        System.out.println("Request allowed for IP: " + clientIp + ". Remaining tokens: " + bucket.getAvailableTokens());
        filterChain.doFilter(request, response);
      } else {
        System.out.println("Too many requests from IP: " + clientIp);
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        response.getWriter().write("Too many requests. Please try again later.");
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private Bucket createNewBucket(String clientIp) {
    Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(10, Duration.ofMinutes(2)));
    return Bucket.builder().addLimit(limit).build();
  }

  private String getClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      return forwardedFor.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}