package com.axonivy.market.logging;

import static com.axonivy.market.constants.CommonConstants.REQUESTED_BY;
import static com.axonivy.market.constants.CommonConstants.REFERER;
import static com.axonivy.market.constants.LoggingConstants.MARKET_WEBSITE;

import com.axonivy.market.service.MatomoService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class TrackApiCallFromNeoAspect {
  @Value("${market.cors.allowed.origin.patterns}")
  private String marketCorsAllowedOriginPatterns;

  private final MatomoService matomoService;
  private List<String> allowedOrigins;

  public TrackApiCallFromNeoAspect(MatomoService matomoService) {this.matomoService = matomoService;}

  @PostConstruct
  public void init() {
    allowedOrigins = Arrays.stream(marketCorsAllowedOriginPatterns.split(","))
        .map(String::trim)
        .map(this::wildcardToRegex)
        .toList();
  }

  @AfterReturning("@annotation(TrackApiCallFromNeo)")
  public void afterTrackedApiCall(JoinPoint joinPoint) {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      String requestedByHeader = request.getHeader(REQUESTED_BY);
      String refererHeader = request.getHeader(REFERER);

      boolean originAllowed = allowedOrigins.stream()
          .anyMatch(regex -> refererHeader != null && refererHeader.matches(regex));
      // Only proceed for NEO DESIGNER
      if (!MARKET_WEBSITE.equals(requestedByHeader) && originAllowed) {
        matomoService.trackEventAsync(request);
      }
    }
  }

  private String wildcardToRegex(String pattern) {
    // Escape dots
    String regex = pattern.replace(".", "\\.");
    // Replace * with .*
    regex = regex.replace("*", ".*");
    // Match the entire string
    return "^" + regex + "$";
  }
}
