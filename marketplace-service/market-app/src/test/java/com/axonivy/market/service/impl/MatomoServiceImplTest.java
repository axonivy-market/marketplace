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
class MatomoServiceImplTest {
  private MatomoTracker matomoTracker;
  private MatomoServiceImpl matomoService;

  @BeforeEach
  void setUp() {
    matomoTracker = mock(MatomoTracker.class);

    when(matomoTracker.sendRequestAsync(any(MatomoRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

    matomoService = new MatomoServiceImpl(matomoTracker);
  }

  @Test
  void testTrackEventAsyncWithQueryParams() {
    HttpServletRequest req = mock(HttpServletRequest.class);

    when(req.getRequestURL()).thenReturn(new StringBuffer("https://market.axonivy.com/marketplace-service/product"));
    when(req.getQueryString()).thenReturn("type=all&sort=standard");
    when(req.getHeader("user-agent")).thenReturn("ChromeBrowser");
    when(req.getHeader("Referer")).thenReturn("https://market.axonivy.com");

    matomoService.trackEventAsync(req);

    var captor = org.mockito.ArgumentCaptor.forClass(MatomoRequest.class);
    verify(matomoTracker, times(1)).sendRequestAsync(captor.capture());

    MatomoRequest matomoReq = captor.getValue();

    Assertions.assertEquals(
            "https://market.axonivy.com/marketplace-service/product?type=all&sort=standard", matomoReq.getActionUrl(),
            "Action URL should contain query parameters when present"
    );

    Assertions.assertEquals("ChromeBrowser", matomoReq.getHeaderUserAgent(),
            "User-Agent header should be correctly forwarded"
    );

    Assertions.assertEquals( "https://market.axonivy.com", matomoReq.getReferrerUrl(),
            "Referer header should be correctly forwarded"
    );
  }

  @Test
  void testTrackEventAsyncWithProductDetails() {
    HttpServletRequest req = mock(HttpServletRequest.class);

    when(req.getRequestURL()).thenReturn(new StringBuffer("https://market.axonivy.com/marketplace-service/api/product-details/ups-connector/12.0.9/bestmatch"));
    when(req.getHeader("user-agent")).thenReturn("ChromeBrowser");
    when(req.getHeader("Referer")).thenReturn("https://market.axonivy.com");

    matomoService.trackEventAsync(req);

    var captor = org.mockito.ArgumentCaptor.forClass(MatomoRequest.class);
    verify(matomoTracker, times(1)).sendRequestAsync(captor.capture());

    MatomoRequest matomoReq = captor.getValue();

    Assertions.assertEquals("https://market.axonivy.com/marketplace-service/api/product-details/ups-connector/12.0.9/bestmatch",
            matomoReq.getActionUrl(),
            "Action URL should contain query parameters when present"
    );
    Assertions.assertEquals( "https://market.axonivy.com", matomoReq.getReferrerUrl(),
            "Referer header should be correctly forwarded"
    );
  }

  @Test
  void testTrackEventAsyncNoQueryParams() {
    HttpServletRequest req = mock(HttpServletRequest.class);

    when(req.getRequestURL()).thenReturn(new StringBuffer("https://market.axonivy.com/marketplace-service/product"));
    when(req.getQueryString()).thenReturn(null);
    when(req.getHeader("user-agent")).thenReturn("ChromeBrowser");
    when(req.getHeader("Referer")).thenReturn(null);

    matomoService.trackEventAsync(req);

    var captor = org.mockito.ArgumentCaptor.forClass(MatomoRequest.class);
    verify(matomoTracker).sendRequestAsync(captor.capture());

    MatomoRequest matomoReq = captor.getValue();

    Assertions.assertEquals("https://market.axonivy.com/marketplace-service/product", matomoReq.getActionUrl(),
            "Action URL should not contain query parameters when they are null"
    );

    Assertions.assertEquals("ChromeBrowser", matomoReq.getHeaderUserAgent(),
            "User-Agent header should be forwarded correctly even without query parameters"
    );

    Assertions.assertNull(matomoReq.getReferrerUrl(),
            "Referrer URL should be null when request contains no Referer header"
    );
  }
}
