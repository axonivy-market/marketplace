package com.axonivy.market.controller;

import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.model.AdminLoginResponse;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.OAuth2Service;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

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

//  @Test
//  void testRequestAccessSuccess() {
//    var mockUser = getMockGithubUser();
//    var mockResponse = new AdminLoginResponse(JWT_TOKEN, mockUser);
//
//    when(oAuth2Service.validateTokenAndGenerateJWT(JWT_TOKEN)).thenReturn(mockResponse);
//
//    ResponseEntity<AdminLoginResponse> response =
//        oAuth2Controller.requestAccess(Map.of(GitHubConstants.Json.TOKEN, JWT_TOKEN));
//
//    assertEquals(HttpStatus.OK, response.getStatusCode(),
//        "Response status should be 200 OK when GitHub login succeeds");
//    assertEquals(mockResponse.token(), Objects.requireNonNull(response.getBody()).token(),
//        "Response body should contain the generated JWT token");
//    assertEquals(mockResponse.user(), response.getBody().user(),
//        "Response body should contain the Github user");
//  }

  @Test
  void testRequestAccessEmptyAuthorizationCode() {
    ResponseEntity<?> response = oAuth2Controller.requestAccess(Map.of(GitHubConstants.Json.TOKEN, ""));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
        "Response status should be 401 UNAUTHORIZED when authorization code is empty.");
  }

//  @Test
//  void testRequestAccessReturnsUnauthorizedWhenServiceReturnsNull() {
//    when(oAuth2Service.validateTokenAndGenerateJWT(JWT_TOKEN)).thenReturn(null);
//
//    ResponseEntity<AdminLoginResponse> response =
//        oAuth2Controller.requestAccess(
//            Map.of(GitHubConstants.Json.TOKEN, JWT_TOKEN));
//
//    assertEquals(
//        HttpStatus.UNAUTHORIZED,
//        response.getStatusCode(),
//        "Expected 401 UNAUTHORIZED when service returns null AdminLoginResponse"
//    );
//
//    assertNull(
//        response.getBody(),
//        "Response body should be null when unauthorized"
//    );
//  }

//  @Test
//  void testRequestAccessReturnsUnauthorizedWhenTokenIsEmpty() {
//    var mockUser = getMockGithubUser();
//    var responseWithEmptyToken =
//        new AdminLoginResponse("", mockUser);
//
//    when(oAuth2Service.validateTokenAndGenerateJWT(JWT_TOKEN))
//        .thenReturn(responseWithEmptyToken);
//
//    ResponseEntity<AdminLoginResponse> response =
//        oAuth2Controller.requestAccess(
//            Map.of(GitHubConstants.Json.TOKEN, JWT_TOKEN));
//
//    assertEquals(
//        HttpStatus.UNAUTHORIZED,
//        response.getStatusCode(),
//        "Expected 401 UNAUTHORIZED when JWT token in response is empty"
//    );
//
//    assertNull(
//        response.getBody(),
//        "Response body should be null when JWT token is empty"
//    );
//  }

  @Test
  void testValidateAuthorizationCode() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getAttribute(AuthorizedAspect.VALIDATED_TOKEN_ATTRIBUTE)).thenReturn(JWT_TOKEN);
    ResponseEntity<?> response = oAuth2Controller.isAuthenticated(mockRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when authorization code is validated.");
  }

  private GithubUser getMockGithubUser() {
    var mockUser = new GithubUser();
    mockUser.setUrl("https://github.com/mockuser");
    mockUser.setName("mockUser");
    mockUser.setUsername("mockUser");
    mockUser.setAvatarUrl("https://avatar.url");

    return mockUser;
  }
}
