package com.axonivy.market.service.impl;

import com.axonivy.market.config.MatomoTrackerBuilder;
import com.axonivy.market.constants.HttpHeaderConstants;
import com.axonivy.market.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoRequests;
import org.matomo.java.tracking.MatomoTracker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.axonivy.market.constants.CommonConstants.*;
import static com.axonivy.market.core.constants.CoreCommonConstants.SLASH;

@Service
@Log4j2
public class MatomoServiceImpl implements MatomoService {

  private static final String NEO_PRODUCT_DASHBOARD = "NEO Product Dashboard";
  private static final String PRODUCT_DETAILS_PREFIX = "/api/product-details/";

  private final MatomoTrackerBuilder matomoTrackerBuilder;

  public MatomoServiceImpl(MatomoTrackerBuilder matomoTrackerBuilder) {
    this.matomoTrackerBuilder = matomoTrackerBuilder;
  }

  @Override
  public void trackEventAsync(HttpServletRequest httpServletRequest) {
    var baseUrl = httpServletRequest.getRequestURL().toString();
    var query = httpServletRequest.getQueryString();
    String requestUrl;

    if (Strings.isNotBlank(query)) {
      requestUrl = baseUrl + "?" + query;
    } else {
      requestUrl = baseUrl;
    }
    String referrerUrl = httpServletRequest.getHeader(REFERER);
    Map<String, String> headers = cloneRequestHeaders(httpServletRequest);
    log.warn("Tracking event for requestUrl={}, referrerUrl={}, headers={}", requestUrl, referrerUrl, headers);
    MatomoRequest req = MatomoRequests.pageView(resolvePageViewName(requestUrl, referrerUrl))
        .actionUrl(requestUrl)
        .headerUserAgent(httpServletRequest.getHeader(USER_AGENT))
        .referrerUrl(referrerUrl)
        .headers(headers)
        .build();
    MatomoTracker tracker = matomoTrackerBuilder.create();
    if (tracker == null) {
      return;
    }
    tracker.sendRequestAsync(req).exceptionally((Throwable ex) -> {
      log.error("Matomo tracking failed to {}", requestUrl, ex);
      return null;
    });
  }

  private Map<String, String> cloneRequestHeaders(HttpServletRequest httpServletRequest) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaderConstants.X_FORWARDED_FOR, httpServletRequest.getRemoteAddr());
    headers.put(HttpHeaderConstants.X_REAL_IP, httpServletRequest.getRemoteAddr());
    return headers;
  }

  private String resolvePageViewName(String requestUrl, String referrerUrl) {
    var prefix = String.format("[%s]", referrerUrl);
    if (requestUrl.contains(PRODUCT_DETAILS_PREFIX)) {
      String productId = extractProductId(requestUrl);
      return prefix + SPACE_SEPARATOR + productId;
    }
    return prefix + SPACE_SEPARATOR + NEO_PRODUCT_DASHBOARD;
  }

  private String extractProductId(String url) {
    int idx = url.indexOf(PRODUCT_DETAILS_PREFIX);
    if (idx == StringUtils.INDEX_NOT_FOUND) {
      return Strings.EMPTY;
    }
    var after = url.substring(idx + PRODUCT_DETAILS_PREFIX.length());
    return after.split(SLASH)[0];
  }
}
