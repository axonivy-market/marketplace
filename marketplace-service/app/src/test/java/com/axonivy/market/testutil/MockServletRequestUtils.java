package com.axonivy.market.testutil;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class MockServletRequestUtils {

  private MockServletRequestUtils() {
  }

  public static MockHttpServletRequest createAndBindMockRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    bindRequest(request);
    return request;
  }

  public static void bindRequest(HttpServletRequest request) {
    ServletRequestAttributes attributes = createRequestAttributes(request);
    RequestContextHolder.setRequestAttributes(attributes);
  }

  public static ServletRequestAttributes createRequestAttributes(HttpServletRequest request) {
    return new ServletRequestAttributes(request);
  }

  public static void resetRequestAttributes() {
    RequestContextHolder.resetRequestAttributes();
  }
}
