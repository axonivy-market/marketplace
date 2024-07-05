package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.axonivy.market.entity.User;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
  void testGitHubLogin() {
    String accessToken = "sampleAccessToken";
    User user = createUserMock();
    String jwtToken = "sampleJwtToken";

    when(gitHubService.getAccessToken(any(), any(), any())).thenReturn(Map.of("access_token", accessToken));
    when(gitHubService.getAndUpdateUser(accessToken)).thenReturn(user);
    when(jwtService.generateToken(user)).thenReturn(jwtToken);

    ResponseEntity<?> response = oAuth2Controller.gitHubLogin(oauth2AuthorizationCode, null);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(Map.of("token", jwtToken), response.getBody());
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
}