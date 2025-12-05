package com.axonivy.market.logging;

import com.axonivy.market.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.axonivy.market.constants.CommonConstants.REQUESTED_BY;
import static com.axonivy.market.constants.LoggingConstants.MARKET_WEBSITE;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackApiCallFromNeoAspectTest {
  @Mock
  private MatomoService matomoService;

  @InjectMocks
  private TrackApiCallFromNeoAspect aspect;

  private MockedStatic<RequestContextHolder> requestContextHolderMock;

  @BeforeEach
  void setup() {
    requestContextHolderMock = mockStatic(RequestContextHolder.class);
  }

  @AfterEach
  void teardown() {
    requestContextHolderMock.close();
  }

  @Test
  void testTrackEventAsyncWhenOriginAllowedAndRequestedByNotMarketWebsite() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(REQUESTED_BY)).thenReturn("ivy");

    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
    when(attributes.getRequest()).thenReturn(request);

    requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

    aspect.afterTrackedApiCall(mock(JoinPoint.class));

    verify(matomoService, times(1)).trackEventAsync(request);
  }

  @Test
  void testShouldNotTrackWhenRequestedByIsMarketWebsite() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(REQUESTED_BY)).thenReturn(MARKET_WEBSITE);

    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
    when(attributes.getRequest()).thenReturn(request);

    requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

    aspect.afterTrackedApiCall(mock(JoinPoint.class));

    verify(matomoService, never()).trackEventAsync(any());
  }

  @Test
  void testShouldNotTrackWhenNoRequestContext() {
    requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

    aspect.afterTrackedApiCall(mock(JoinPoint.class));

    verify(matomoService, never()).trackEventAsync(any());
  }
}
