package com.axonivy.market.logging;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.LoggingConstants;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.util.LoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggableAspectTest {

  @Mock
  private HttpServletRequest request;

  private LoggableAspect loggableAspect;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    loggableAspect = new LoggableAspect();
    loggableAspect.logFilePath = Files.createTempDirectory("logs").toString();
  }

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void testLogFileCreation() throws Exception {
    mockRequestAttributes(LoggingConstants.MARKET_WEBSITE, "test-agent");
    MethodSignature signature = mockMethodSignature();

    loggableAspect.logMethodCall(mockJoinPoint(signature));
    Path logFilePath = Path.of(loggableAspect.logFilePath, "log-" + LoggingUtils.getCurrentDate() + ".xml");
    assertTrue(Files.exists(logFilePath), "Log file should be created");

    String content = Files.readString(logFilePath);
    assertTrue(content.contains(LoggingConstants.LOG_START), "Log file should contain log");
    assertTrue(content.contains(LoggingConstants.ENTRY_START), "Log file should contain log entry");
  }

  @Test
  void testMissingHeaderException() {
    mockRequestAttributes("invalid-source", "mock-agent");
    MethodSignature signature = mockMethodSignature();

    assertThrows(MissingHeaderException.class,
        () -> loggableAspect.logMethodCall(mockJoinPoint(signature)),
        "Expected MissingHeaderException to be thrown when headers are invalid");
  }

  private JoinPoint mockJoinPoint(MethodSignature signature) {
    JoinPoint joinPoint = mock(JoinPoint.class);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
    return joinPoint;
  }

  private void mockRequestAttributes(String requestedBy, String userAgent) {
    when(request.getHeader(CommonConstants.REQUESTED_BY)).thenReturn(requestedBy);
    when(request.getHeader(CommonConstants.USER_AGENT)).thenReturn(userAgent);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  private MethodSignature mockMethodSignature() {
    MethodSignature signature = mock(MethodSignature.class);
    when(signature.getMethod()).thenReturn(this.getClass().getMethods()[0]);
    return signature;
  }

}
