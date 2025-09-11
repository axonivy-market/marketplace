package com.axonivy.market.config;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.http.HttpMethod;

class MarketHeaderInterceptorTest {
  private MarketHeaderInterceptor interceptor;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @BeforeEach
  void setUp() {
    interceptor = new MarketHeaderInterceptor();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
  }

  @Test
  void testPreHandleWithOptionsRequestShouldReturnTrue() throws Exception {
    when(request.getMethod()).thenReturn(HttpMethod.OPTIONS.name());

    boolean result = interceptor.preHandle(request, response, new Object());

    assertTrue(result, "preHandle should return true for OPTIONS requests");
  }

  @Test
  void testPreHandleGetRequestWithoutHeaderShouldReturnTrue() throws Exception {
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(request.getHeader(CommonConstants.REQUESTED_BY)).thenReturn(null);

    boolean result = interceptor.preHandle(request, response, new Object());

    assertTrue(result, "preHandle should return true for GET requests even without header");
  }

  @Test
  void testPreHandlePostRequestWithHeaderShouldReturnTrue() throws Exception {
    when(request.getMethod()).thenReturn(HttpMethod.POST.name());
    when(request.getHeader(CommonConstants.REQUESTED_BY)).thenReturn("test-client");

    boolean result = interceptor.preHandle(request, response, new Object());

    assertTrue(result, "preHandle should return true for non-GET requests with header present");
  }

  @Test
  void testPreHandlePostRequestWithoutHeaderShouldThrowException() {
    when(request.getMethod()).thenReturn(HttpMethod.POST.name());
    when(request.getHeader(CommonConstants.REQUESTED_BY)).thenReturn(null);

    MissingHeaderException exception = assertThrows(
        MissingHeaderException.class,
        () -> interceptor.preHandle(request, response, new Object()),
        "preHandle should throw MissingHeaderException for non-GET requests without header"
    );

    assertNotNull(exception, "Exception should not be null when header is missing");
  }
}
