package com.axonivy.market.controller;

import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.OAuth2Service;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ControllerTest {

  private static final String JWT_TOKEN = "sampleJwtToken";

  @Mock
  private OAuth2Service oAuth2Service;
  @InjectMocks
  private OAuth2Controller oAuth2Controller;

  private Oauth2AuthorizationCode oauth2AuthorizationCode;

  @BeforeEach
  void setup() {
    oauth2AuthorizationCode = new Oauth2AuthorizationCode();
    oauth2AuthorizationCode.setCode("sampleCode");
  }

  @Test
  void testGitHubLoginSuccess() {
    when(oAuth2Service.loginToGitHubAndGetJWT(oauth2AuthorizationCode)).thenReturn(JWT_TOKEN);
    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode);

    assertEquals(200, response.getStatusCode().value(),
        "Response status should be 200 OK when GitHub login succeeds");
    assertEquals(Map.of("token", JWT_TOKEN), response.getBody(),
        "Response body should contain the generated JWT token");
  }

  @Test
  void testGitHubLoginOauth2ExchangeCodeException() {
    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
        "Response status should be 401 UNAUTHORIZED when OAuth2 exchange fails");
  }

  @Test
  void testRequestAccessSuccess() {
    when(oAuth2Service.validateTokenAndGenerateJWT(JWT_TOKEN)).thenReturn(JWT_TOKEN);
    ResponseEntity<?> response = oAuth2Controller.requestAccess(Map.of(GitHubConstants.Json.TOKEN, JWT_TOKEN));

    assertEquals(200, response.getStatusCode().value(),
        "Response status should be 200 OK when GitHub login succeeds");
    assertEquals(Map.of("token", JWT_TOKEN), response.getBody(),
        "Response body should contain the generated JWT token");
  }

  @Test
  void testRequestAccessEmptyAuthorizationCode() {
    ResponseEntity<?> response = oAuth2Controller.requestAccess(Map.of(GitHubConstants.Json.TOKEN, ""));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
        "Response status should be 401 UNAUTHORIZED when authorization code is empty.");
  }

  @Test
  void testValidateAuthorizationCode() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getAttribute(AuthorizedAspect.VALIDATED_TOKEN_ATTRIBUTE)).thenReturn(JWT_TOKEN);
    ResponseEntity<?> response = oAuth2Controller.isAuthenticated(mockRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when authorization code is validated.");
  }
}
