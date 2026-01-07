package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceImplTest {
  private Oauth2AuthorizationCode oauth2AuthorizationCode;

  @Mock
  private GitHubService gitHubService;
  @Mock
  private JwtService jwtService;
  @InjectMocks
  private OAuth2ServiceImpl oAuth2Service;

  @BeforeEach
  void setup() {
    oauth2AuthorizationCode = new Oauth2AuthorizationCode();
    oauth2AuthorizationCode.setCode("sampleCode");
  }

  @Test
  void testGitHubLoginSuccess() throws Exception {
    String accessToken = "sampleAccessToken";
    GithubUser githubUser = createUserMock();
    String jwtToken = "sampleJwtToken";

    when(gitHubService.getAccessToken(any(), any())).thenReturn(createGitHubAccessTokenResponseMock());
    when(gitHubService.getAndUpdateUser(accessToken)).thenReturn(githubUser);
    when(jwtService.generateToken(githubUser, accessToken)).thenReturn(jwtToken);

    String jwt = oAuth2Service.loginToGitHubAndGetJWT(oauth2AuthorizationCode);

    assertTrue(ObjectUtils.isNotEmpty(jwt), "Response status should be 200 OK when GitHub login succeeds");
    assertEquals(jwtToken, jwt, "Response body should contain the generated JWT token");
  }

  @Test
  void testGitHubLoginOauth2ExchangeCodeException() throws Exception {
    when(gitHubService.getAccessToken(any(), any())).thenThrow(
        new Oauth2ExchangeCodeException("invalid_grant", "Invalid authorization code"));

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> oAuth2Service.loginToGitHubAndGetJWT(oauth2AuthorizationCode),
        "Response status should be 400 BAD_REQUEST when OAuth2 exchange code fails");

    assertEquals("BAD_REQUEST", exception.getError(), "Response error should be BAD_REQUEST");
    assertEquals("Invalid authorization code", exception.getErrorDescription(),
      "Error description should match");
  }

  @Test
  void testGitHubLoginGeneralException() throws Exception {
    when(gitHubService.getAccessToken(any(), any())).thenThrow(new RuntimeException("Unexpected error"));

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> oAuth2Service.loginToGitHubAndGetJWT(oauth2AuthorizationCode),
        "Response status should be 500 INTERNAL_SERVER_ERROR when a general exception occurs");

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name(), exception.getError(),
        "Response status should be 500 INTERNAL_SERVER_ERROR when a general exception occurs");
  }

  @Test
  void testRequestAccessSuccess() {
    String mockToken = "Bearer sampleAccessToken";
    when(jwtService.generateJWTFromGitHubToken(any())).thenReturn("mockToken");

    var token = oAuth2Service.validateTokenAndGenerateJWT(mockToken);
    assertTrue(ObjectUtils.isNotEmpty(token),
        "Response status should be 200 OK when GitHub login succeeds");
  }

  @Test
  void testRequestAccessGeneralException() {
    String mockToken = "Bearer sampleAccessToken";
    doThrow(new UnauthorizedException("", "Unexpected error")).when(gitHubService)
        .validateUserInOrganizationAndTeam(any(), any(), any());

    UnauthorizedException exception = assertThrows(
        UnauthorizedException.class,
        () -> oAuth2Service.validateTokenAndGenerateJWT(mockToken),
        "Response status should be 401 UNAUTHORIZED when a general exception occurs");
    assertEquals("Unexpected error", exception.getMessage(),
        "Response status should be 401 UNAUTHORIZED when a general exception occurs");
  }

  private GithubUser createUserMock() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("userId");
    githubUser.setUsername("username");
    githubUser.setName("User Name");
    githubUser.setAvatarUrl("http://avatar.url");
    githubUser.setProvider("github");
    return githubUser;
  }

  private GitHubAccessTokenResponse createGitHubAccessTokenResponseMock() {
    GitHubAccessTokenResponse gitHubAccessTokenResponse = new GitHubAccessTokenResponse();
    gitHubAccessTokenResponse.setAccessToken("sampleAccessToken");
    return gitHubAccessTokenResponse;
  }
}
