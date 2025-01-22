package com.axonivy.market.controller;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2ControllerTest {

  @Mock
  private GitHubService gitHubService;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private OAuth2Controller oAuth2Controller;

  private Oauth2AuthorizationCode oauth2AuthorizationCode;

  @BeforeEach
  void setup() {
    oauth2AuthorizationCode = new Oauth2AuthorizationCode();
    oauth2AuthorizationCode.setCode("sampleCode");
  }

  @Test
  void testGitHubLogin_Success() throws Exception {
    String accessToken = "sampleAccessToken";
    User user = createUserMock();
    String jwtToken = "sampleJwtToken";

    when(gitHubService.getAccessToken(any(), any())).thenReturn(createGitHubAccessTokenResponseMock());
    when(gitHubService.getAndUpdateUser(accessToken)).thenReturn(user);
    when(jwtService.generateToken(user)).thenReturn(jwtToken);

    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(Map.of("token", jwtToken), response.getBody());
  }

  @Test
  void testGitHubLogin_Oauth2ExchangeCodeException() throws Exception {
    when(gitHubService.getAccessToken(any(), any())).thenThrow(
        new Oauth2ExchangeCodeException("invalid_grant", "Invalid authorization code"));

    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Map<String, String> body = (Map<String, String>) response.getBody();
    assertEquals("invalid_grant", body.get(CommonConstants.ERROR));
    assertEquals("Invalid authorization code", body.get(CommonConstants.MESSAGE));
  }

  @Test
  void testGitHubLogin_GeneralException() throws Exception {
    when(gitHubService.getAccessToken(any(), any())).thenThrow(new RuntimeException("Unexpected error"));

    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Map<String, String> body = (Map<String, String>) response.getBody();
    assertTrue(body.containsKey(CommonConstants.MESSAGE));
    assertEquals("Unexpected error", body.get(CommonConstants.MESSAGE));
  }

  @Test
  void testGitHubLogin_EmptyAuthorizationCode() {
    oauth2AuthorizationCode.setCode(null);

    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Map<String, String> body = (Map<String, String>) response.getBody();
    assertTrue(body.containsKey(CommonConstants.MESSAGE));
  }

  private User createUserMock() {
    User user = new User();
    user.setId("userId");
    user.setUsername("username");
    user.setName("User Name");
    user.setAvatarUrl("http://avatar.url");
    user.setProvider("github");
    return user;
  }

  private GitHubAccessTokenResponse createGitHubAccessTokenResponseMock() {
    GitHubAccessTokenResponse gitHubAccessTokenResponse = new GitHubAccessTokenResponse();
    gitHubAccessTokenResponse.setAccessToken("sampleAccessToken");
    return gitHubAccessTokenResponse;
  }
}
