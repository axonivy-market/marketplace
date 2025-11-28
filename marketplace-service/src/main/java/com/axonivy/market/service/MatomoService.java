package com.axonivy.market.service;

import jakarta.servlet.http.HttpServletRequest;

public interface MatomoService {
  void trackEventAsync(HttpServletRequest httpRequest);
}
