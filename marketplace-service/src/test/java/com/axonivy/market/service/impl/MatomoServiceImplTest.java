package com.axonivy.market.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoTracker;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatomoServiceImplTest {
  private MatomoTracker matomoTracker;
  private MatomoServiceImpl matomoService;

  @BeforeEach
  void setUp() {
    matomoTracker = mock(MatomoTracker.class);

    // Prepare default stub for async call
    when(matomoTracker.sendRequestAsync(any(MatomoRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    matomoService = new MatomoServiceImpl(matomoTracker);
  }

  @Test
  void testTrackEventAsync_WithQueryParams() {
    HttpServletRequest req = mock(HttpServletRequest.class);

    when(req.getRequestURL()).thenReturn(new StringBuffer("https://market.axonivy.com/marketplace-service/product"));
    when(req.getQueryString()).thenReturn("type=all&sort=standard");
    when(req.getHeader("User-Agent")).thenReturn("ChromeBrowser");
    when(req.getHeader("Referer")).thenReturn("https://market.axonivy.com");

    matomoService.trackEventAsync(req);

    // capture argument
    var captor = org.mockito.ArgumentCaptor.forClass(MatomoRequest.class);
    verify(matomoTracker, times(1)).sendRequestAsync(captor.capture());

    MatomoRequest matomoReq = captor.getValue();

    // Validate final URL
    assert matomoReq.getActionUrl().equals(
        "https://market.axonivy.com/marketplace-service/product?type=all&sort=standard");
    assert matomoReq.getHeaderUserAgent().equals("ChromeBrowser");
    assert matomoReq.getReferrerUrl().equals("https://market.axonivy.com");
  }

  @Test
  void testTrackEventAsync_NoQueryParams() {
    HttpServletRequest req = mock(HttpServletRequest.class);

    when(req.getRequestURL()).thenReturn(new StringBuffer("https://market.axonivy.com/marketplace-service/product"));
    when(req.getQueryString()).thenReturn(null);
    when(req.getHeader("User-Agent")).thenReturn("ChromeBrowser");
    when(req.getHeader("Referer")).thenReturn(null);

    matomoService.trackEventAsync(req);

    var captor = org.mockito.ArgumentCaptor.forClass(MatomoRequest.class);
    verify(matomoTracker).sendRequestAsync(captor.capture());

    MatomoRequest matomoReq = captor.getValue();

    Assertions.assertEquals("https://market.axonivy.com/marketplace-service/product", matomoReq.getActionUrl());
    Assertions.assertEquals("ChromeBrowser", matomoReq.getHeaderUserAgent());
    Assertions.assertNull(req.getHeader("Referer"));
  }
}
