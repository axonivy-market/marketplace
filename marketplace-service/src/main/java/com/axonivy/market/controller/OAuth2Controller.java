package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.AUTH;
import static com.axonivy.market.constants.RequestMappingConstants.GIT_HUB_LOGIN;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.User;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;

@RestController
@RequestMapping(AUTH)
public class OAuth2Controller {

  private final GitHubProperty gitHubProperty;

  private final GitHubService gitHubService;

  private final JwtService jwtService;

  public OAuth2Controller(GitHubService gitHubService, JwtService jwtService, GitHubProperty gitHubProperty) {
    this.gitHubService = gitHubService;
    this.jwtService = jwtService;
    this.gitHubProperty = gitHubProperty;
  }

  @PostMapping(GIT_HUB_LOGIN)
  public ResponseEntity<Map<String, String>> gitHubLogin(@RequestBody Oauth2AuthorizationCode oauth2AuthorizationCode) {
    String accessToken = EMPTY;
    try {
      GitHubAccessTokenResponse tokenResponse = gitHubService.getAccessToken(oauth2AuthorizationCode.getCode(),
          gitHubProperty);
      accessToken = tokenResponse.getAccessToken();
    } catch (Exception e) {
      return new ResponseEntity<>(Map.of(e.getClass().getName(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    User user = gitHubService.getAndUpdateUser(accessToken);
    String jwtToken = jwtService.generateToken(user);
    return new ResponseEntity<>(Collections.singletonMap(GitHubConstants.Json.TOKEN, jwtToken), HttpStatus.OK);
  }
}