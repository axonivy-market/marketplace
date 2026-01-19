package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.OAuth2Service;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.AUTH;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_LOGIN;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_REQUEST_ACCESS;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping(AUTH)
@Hidden
public class OAuth2Controller {

  private final OAuth2Service oAuth2Service;

  @PostMapping(GITHUB_LOGIN)
  public ResponseEntity<Map<String, String>> gitHubLogin(@RequestBody Oauth2AuthorizationCode oauth2AuthorizationCode) {
    String jwt = oAuth2Service.loginToGitHubAndGetJWT(oauth2AuthorizationCode);
    return responseJWTData(jwt);
  }

  @PostMapping(GITHUB_REQUEST_ACCESS)
  public ResponseEntity<Map<String, String>> requestAccess(@RequestBody Map<String, String> token) {
    String jwt = oAuth2Service.validateTokenAndGenerateJWT(token.get(GitHubConstants.Json.TOKEN));
    return responseJWTData(jwt);
  }

  private ResponseEntity<Map<String, String>> responseJWTData(String jwt) {
    if (ObjectUtils.isEmpty(jwt)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    return new ResponseEntity<>(Map.of(GitHubConstants.Json.TOKEN, jwt), HttpStatus.OK);
  }
}
