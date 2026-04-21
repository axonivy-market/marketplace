package com.axonivy.market.service.impl;

import com.axonivy.market.constants.HttpHeaderConstants;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoRequests;
import org.matomo.java.tracking.MatomoTracker;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import static com.axonivy.market.constants.CommonConstants.*;
import static com.axonivy.market.core.constants.CoreCommonConstants.SLASH;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class MatomoServiceImpl implements MatomoService {

  private static final String NEO_PRODUCT_DASHBOARD = "NEO Product Dashboard";
  private static final String PRODUCT_DETAILS_PREFIX = "/api/product-details/";

  private final MatomoTracker matomoTracker;

  public MatomoServiceImpl(MatomoTracker matomoTracker) {
    this.matomoTracker = matomoTracker;
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

    matomoTracker.sendRequestAsync(req).exceptionally((Throwable ex) -> {
      log.error("Matomo tracking failed to {}", requestUrl, ex);
      return null;
    });
  }

  private Map<String, String> cloneRequestHeaders(HttpServletRequest httpServletRequest) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaderConstants.X_FORWARDED_FOR, httpServletRequest.getRemoteAddr());
                  log.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ matomo header {} with value {}", HttpHeaderConstants.X_FORWARDED_FOR, httpServletRequest.getRemoteAddr());

    Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        Enumeration<String> values = httpServletRequest.getHeaders(name);
        List<String> list = values != null ? Collections.list(values) : Collections.emptyList();
        if (!list.isEmpty()) {
          String value = String.join(CoreCommonConstants.COMMA, list);
          if (StringUtils.isNotBlank(value)) {
            if (StringUtils.equalsAnyIgnoreCase(name, HttpHeaders.AUTHORIZATION, HttpHeaders.COOKIE,
                HttpHeaders.SET_COOKIE) && StringUtils.isNotBlank(value)) {
              log.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ matomo header {} with value {}", name, value);
              headers.put(name, value);
            }
          }
        }
      }
    }
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
