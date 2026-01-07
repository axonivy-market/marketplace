package com.axonivy.market.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.constants.RequestParamConstants;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthorizedAspectTest {
  @Mock
  private HttpServletRequest request;
  
  @Mock
  private JwtService jwtService;
  
  @Mock
  private GitHubService gitHubService;
  
  @Mock
  private ProceedingJoinPoint joinPoint;
  
  @InjectMocks
  private AuthorizedAspect authorizedAspect;
  
  @BeforeEach
  void setup() {
    ServletRequestAttributes attributes = new ServletRequestAttributes(request);
    RequestContextHolder.setRequestAttributes(attributes);
  }
  
  @Test
  void testUnauthorizedThrowsException() throws Throwable {
    when(request.getHeader(AUTHORIZATION)).thenReturn(null);
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION)).thenReturn(null);
    
    assertThrows(Oauth2ExchangeCodeException.class, 
        () -> authorizedAspect.validateAuthorization(joinPoint),
        "Should throw Oauth2ExchangeCodeException when no authorization header is present");
  }
  
  @Test
  void testAuthorizedSuccess() throws Throwable {
    when(request.getHeader(RequestParamConstants.X_AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(jwtService.getRawAccessToken("Bearer valid-token")).thenReturn("valid-token");
    when(joinPoint.proceed()).thenReturn("success");
    
    Object result = authorizedAspect.validateAuthorization(joinPoint);
    
    assertEquals("success", result, "Should return the result from proceed when authorized");
    verify(joinPoint).proceed();
  }
}
