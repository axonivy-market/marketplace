package com.axonivy.market.service.impl;

import com.axonivy.market.service.MatomoService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoRequests;
import org.matomo.java.tracking.MatomoTracker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class MatomoServiceImpl implements MatomoService {
  private final MatomoTracker matomoTracker;

  public MatomoServiceImpl(MatomoTracker matomoTracker) {this.matomoTracker = matomoTracker;}

  @Override
  public void trackEventAsync(HttpServletRequest httpServletRequest) {
    String baseUrl = httpServletRequest.getRequestURL().toString();
    String query = httpServletRequest.getQueryString();
    String requestUrl = query != null ? baseUrl + "?" + query : baseUrl;

    MatomoRequest req = MatomoRequests.pageView("NEO designer")
        .actionUrl(requestUrl)
        .headerUserAgent(httpServletRequest.getHeader("User-Agent"))
        .referrerUrl(httpServletRequest.getHeader("Referer"))
        .build();

    matomoTracker.sendRequestAsync(req).exceptionally(ex -> {
      log.error("Matomo tracking failed to {}", requestUrl, ex);
      return null;
    });
  }
}
