package com.axonivy.market.controller;

import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.JwtService;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityMonitorControllerTest {
  @Mock
  private JwtService jwtService;

  @Mock
  private GitHubService gitHubService;

  @InjectMocks
  private SecurityMonitorController securityMonitorController;

  @Test
  void testGetGitHubMarketplaceSecurity() throws IOException {
    Claims mockClaims = createMockClaims();
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    when(jwtService.validateToken("TOKEN_SAMPLE")).thenReturn(true);
    when(jwtService.getClaimsFromToken("TOKEN_SAMPLE")).thenReturn(mockClaims);
    ProductSecurityInfo product1 = new ProductSecurityInfo("product1", false, "public", true, new Date(), "abc123",
        null, null, null);

    ProductSecurityInfo product2 = new ProductSecurityInfo("product2", false, "private", false, new Date(), "def456",
        null, null, null);
    List<ProductSecurityInfo> mockSecurityInfoList = Arrays.asList(product1, product2);

    when(gitHubService.getSecurityDetailsForAllProducts(anyString(), anyString())).thenReturn(mockSecurityInfoList);

    ResponseEntity<List<ProductSecurityInfo>> expectedResponse = new ResponseEntity<>(mockSecurityInfoList,
        HttpStatus.OK);

    ResponseEntity<Object> actualResponse = securityMonitorController.getGitHubMarketplaceSecurity(request);

    assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode(),
        "HTTP status codes do not match. Expected " + expectedResponse.getStatusCode() +
            " but got " + actualResponse.getStatusCode());
    assertEquals(expectedResponse.getBody(), actualResponse.getBody(),
        "Response body does not match expected security info list.");
  }

  @Test
  void testGetGitHubMarketplaceSecurityShouldReturnUnauthorizedWhenInvalidToken() {
    Claims mockClaims = createMockClaims();
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    when(jwtService.validateToken("TOKEN_SAMPLE")).thenReturn(true);
    when(jwtService.getClaimsFromToken("TOKEN_SAMPLE")).thenReturn(mockClaims);
    doThrow(new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText())).when(gitHubService)
        .validateUserInOrganizationAndTeam(any(String.class), any(String.class), any(String.class));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> securityMonitorController.getGitHubMarketplaceSecurity(request),
        "Expected UnauthorizedException to be thrown when token is invalid, but no exception was thrown.");

    assertEquals(ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), exception.getMessage(),
        "UnauthorizedException message does not match expected help text.");
  }

  private Claims createMockClaims() {
    Claims claims = new io.jsonwebtoken.impl.DefaultClaims();
    claims.setSubject("USER_ID_SAMPLE");
    return claims;
  }
}
