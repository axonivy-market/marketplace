package com.axonivy.market.config;

import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.axonivy.market.constants.HttpHeaderConstants.X_REAL_IP;

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

    var paths = settingService.getStringValueByKey(AppSettingKey.LIMITED_REQUEST_PATHS);
    List<String> requestPaths = Arrays.stream(paths.split(CoreCommonConstants.COMMA))
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
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Too many requests. Please try again later.");
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private Bucket createNewBucket(String clientIp) {
    var capacity = settingService.getLongValueByKey(AppSettingKey.CLICK_CAPACITY);
    Bandwidth limit = Bandwidth.builder()
        .capacity(capacity)
        .refillGreedy(capacity, Duration.ofMinutes(1))
        .build();
    return Bucket.builder().addLimit(limit).build();
  }

  private static String getClientIp(HttpServletRequest request) {
    String realIp = StringUtils.trimToNull(request.getHeader(X_REAL_IP));
    return isValidIp(realIp) ? realIp : request.getRemoteAddr();
  }

  private static boolean isValidIp(String ip) {
    InetAddressValidator validator = InetAddressValidator.getInstance();
    return ip != null && (validator.isValidInet4Address(ip) || validator.isValidInet6Address(ip));
  }
}
