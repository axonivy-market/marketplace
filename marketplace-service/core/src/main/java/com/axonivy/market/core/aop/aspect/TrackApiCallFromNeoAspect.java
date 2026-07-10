package com.axonivy.market.core.aop.aspect;


import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
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

  @AfterReturning("@annotation(com.axonivy.market.core.aop.annotation.TrackApiCallFromNeo)")
  public void afterTrackedApiCall() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      String requestedByHeader = request.getHeader(CoreCommonConstants.REQUESTED_BY);
      if (CoreCommonConstants.IVY_HEADER.equals(requestedByHeader)) {
        matomoService.trackEventAsync(request);
      }
    }
  }
}
