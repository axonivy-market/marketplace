package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.User;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class OAuth2Controller {

  @Value("${spring.security.oauth2.client.registration.github.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.github.client-secret}")
  private String clientSecret;

  private final GitHubService gitHubService;

  private final JwtService jwtService;

  public OAuth2Controller(GitHubService gitHubService, JwtService jwtService) {
    this.gitHubService = gitHubService;
    this.jwtService = jwtService;
  }

  @PostMapping("/github/login")
  public ResponseEntity<Object> gitHubLogin(@RequestBody Oauth2AuthorizationCode oauth2AuthorizationCode) {
    GitHubAccessTokenResponse tokenResponse = gitHubService.getAccessToken(oauth2AuthorizationCode.getCode(), clientId,
        clientSecret);
    String accessToken = tokenResponse.getAccessToken();

    User user = gitHubService.getAndUpdateUser(accessToken);

    String jwtToken = jwtService.generateToken(user);

    return ResponseEntity.ok().body(Collections.singletonMap(GitHubConstants.Json.TOKEN, jwtToken));
  }
}