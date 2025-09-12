package com.axonivy.market.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

class LimitCallingConfigTest {
  private LimitCallingConfig filter;

  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;
  private StringWriter responseWriter;

  @BeforeEach
  void setUp() throws IOException {
    filter = new LimitCallingConfig();

    ReflectionTestUtils.setField(filter, "capacity", 2);
    ReflectionTestUtils.setField(filter, "requestPaths", List.of("/api/test"));

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    chain = mock(FilterChain.class);

    responseWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
  }

  @Test
  void testShouldAllowRequestWhenPathNotLimited() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/public/data");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    filter.doFilterInternal(request, response, chain);

    verify(chain, times(1)).doFilter(request, response);
    assertThat(responseWriter.toString())
        .as("Response writer should be empty since no blocking occurred")
        .isEmpty();
  }

  @Test
  void testShouldAllowRequestWhenWithinCapacity() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/test/resource");
    when(request.getRemoteAddr()).thenReturn("192.168.1.10");

    filter.doFilterInternal(request, response, chain);

    verify(chain, times(1)).doFilter(request, response);
    assertThat(responseWriter.toString())
        .as("Response writer should remain empty for allowed requests")
        .isEmpty();
  }

  @Test
  void testShouldBlockRequestWhenExceedingCapacity() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/test/resource");
    when(request.getRemoteAddr()).thenReturn("10.0.0.5");

    // consume all available tokens (capacity = 2)
    filter.doFilterInternal(request, response, chain);
    filter.doFilterInternal(request, response, chain);

    // 3rd request should be blocked
    filter.doFilterInternal(request, response, chain);

    verify(response, times(1))
        .setStatus(HttpServletResponse.SC_BAD_GATEWAY);
    assertThat(responseWriter.toString())
        .as("Response writer should contain the rejection message after exceeding capacity")
        .contains("Too many requests");
  }

  @Test
  void testShouldReturnFirstIpWhenXForwardedForHasMultipleIps() {
//    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.45, 10.0.0.1, proxy");
    when(request.getRemoteAddr()).thenReturn("192.168.0.5");

    String result = ReflectionTestUtils.invokeMethod(LimitCallingConfig.class,
        "getClientIp", request);

    assertThat(result)
        .as("Should return the first IP from X-Forwarded-For header")
        .isEqualTo("203.0.113.45");
  }
}
