package com.axonivy.market.service;

import jakarta.servlet.http.HttpServletRequest;

public interface MatomoService {

  /**
   * <p>
   * Track event asynchronization
   * </p>
   *
   * @param  httpRequest
   *              type {@link HttpServletRequest}
   * @return {@link }
   * @author tvtphuc
   */
  void trackEventAsync(HttpServletRequest httpRequest);
}
