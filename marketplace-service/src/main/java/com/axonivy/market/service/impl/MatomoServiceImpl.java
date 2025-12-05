package com.axonivy.market.service.impl;

import com.axonivy.market.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoRequests;
import org.matomo.java.tracking.MatomoTracker;
import org.springframework.stereotype.Service;

import static com.axonivy.market.constants.CommonConstants.*;

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
    MatomoRequest req = MatomoRequests.pageView(resolvePageViewName(requestUrl, referrerUrl))
        .actionUrl(requestUrl)
        .headerUserAgent(httpServletRequest.getHeader(USER_AGENT))
        .referrerUrl(referrerUrl)
        .build();

    matomoTracker.sendRequestAsync(req).exceptionally((Throwable ex) -> {
      log.error("Matomo tracking failed to {}", requestUrl, ex);
      return null;
    });
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
    if (idx == -1) {
      return Strings.EMPTY;
    }
    var after = url.substring(idx + PRODUCT_DETAILS_PREFIX.length());
    return after.split(SLASH)[0];
  }
}
