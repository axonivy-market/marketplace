package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.axonivy.market.constants.HttpHeaderConstants.X_FORWARDED_FOR;

@Log4j2
@Component
@RequiredArgsConstructor
public class LimitCallingConfig extends OncePerRequestFilter {

  private final Map<String, Bucket> clientBuckets = new ConcurrentHashMap<>();
  private final AppSettingService settingService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = getClientIp(request);
    String apiPath = request.getRequestURI();

    String paths = settingService.getValueByKey(AppSettingKey.LIMITED_REQUEST_PATHS);
    List<String> requestPaths = Arrays.stream(paths.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();

    boolean isRequestPathMatched = requestPaths.stream().anyMatch(apiPath::contains);
    if (isRequestPathMatched) {
      var bucket = clientBuckets.computeIfAbsent(clientIp, this::createNewBucket);

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
    var capacity = Long.parseLong(settingService.getValueByKey(AppSettingKey.CLICK_CAPACITY));
    Bandwidth limit = Bandwidth.builder()
        .capacity(capacity)
        .refillGreedy(capacity, Duration.ofMinutes(1))
        .build();
    return Bucket.builder().addLimit(limit).build();
  }

  private static String getClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader(X_FORWARDED_FOR);
    if (StringUtils.isNotEmpty(forwardedFor)) {
      return forwardedFor.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}
