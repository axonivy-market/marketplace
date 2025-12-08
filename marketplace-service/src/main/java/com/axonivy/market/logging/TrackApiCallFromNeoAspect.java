package com.axonivy.market.logging;

import static com.axonivy.market.constants.CommonConstants.*;

import com.axonivy.market.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class TrackApiCallFromNeoAspect {
  private final MatomoService matomoService;

  public TrackApiCallFromNeoAspect(MatomoService matomoService) {
    this.matomoService = matomoService;
  }

  @AfterReturning("@annotation(TrackApiCallFromNeo)")
  public void afterTrackedApiCall(JoinPoint joinPoint) {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      String requestedByHeader = request.getHeader(REQUESTED_BY);
      if (IVY_HEADER.equals(requestedByHeader)) {
        matomoService.trackEventAsync(request);
      }
    }
  }
}
