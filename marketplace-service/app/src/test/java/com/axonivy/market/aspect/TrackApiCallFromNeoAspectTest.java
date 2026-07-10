package com.axonivy.market.aspect;

import com.axonivy.market.core.aop.annotation.TrackApiCallFromNeo;
import com.axonivy.market.core.aop.aspect.TrackApiCallFromNeoAspect;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.service.MatomoService;
import com.axonivy.market.testutil.MockServletRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;

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

  private static class AnnotatedFixture {
    @TrackApiCallFromNeo()
    public void appEndpoint() {}
  }

  private static TrackApiCallFromNeo getTrackApiCallFromNeo() throws NoSuchMethodException {
    return AnnotatedFixture.class
        .getMethod("appEndpoint")
        .getAnnotation(TrackApiCallFromNeo.class);
  }

  @Test
  void testTrackEventAsyncWhenOriginAllowedAndRequestedByNotMarketWebsite() throws NoSuchMethodException {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(CoreCommonConstants.REQUESTED_BY)).thenReturn("ivy");

    requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
        .thenReturn(MockServletRequestUtils.createRequestAttributes(request));

    aspect.afterTrackedApiCall();

    verify(matomoService, times(1)).trackEventAsync(request);
  }

  @Test
  void testShouldNotTrackWhenRequestedByIsMarketWebsite() throws NoSuchMethodException {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(CoreCommonConstants.REQUESTED_BY)).thenReturn(CoreCommonConstants.MARKET_WEBSITE);

    requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
        .thenReturn(MockServletRequestUtils.createRequestAttributes(request));

    aspect.afterTrackedApiCall();

    verify(matomoService, never()).trackEventAsync(any());
  }

  @Test
  void testShouldNotTrackWhenNoRequestContext() throws NoSuchMethodException {
    requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

    aspect.afterTrackedApiCall();

    verify(matomoService, never()).trackEventAsync(any());
  }
}
