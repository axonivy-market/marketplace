package com.axonivy.market.service;

import jakarta.servlet.http.HttpServletRequest;

public interface MatomoService {

  /**
   * <p>
   * Asynchronously tracks user interactions and page views to Matomo analytics platform. Captures HTTP request
   * information (URL, referrer, user agent) and sends analytics data without blocking the request processing.
   * Used for tracking marketplace usage patterns, popular products, and user behavior.
   * </p>
   *
   * @param  httpRequest
   *              type {@link HttpServletRequest} - the HTTP request object containing user interaction details
   * @return void - tracking is performed asynchronously; no return value
   * @author tvtphuc
   */
  void trackEventAsync(HttpServletRequest httpRequest);
}
