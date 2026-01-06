package com.axonivy.market.service.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;
import com.axonivy.market.service.OAuth2Service;
import com.axonivy.market.util.validator.AuthorizationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuth2ServiceImpl implements OAuth2Service {
  private final GitHubProperty gitHubProperty;
  private final GitHubService gitHubService;
  private final JwtService jwtService;

  @Override
  public String loginToGitHubAndGetJWT(Oauth2AuthorizationCode oauth2AuthorizationCode) {
    try {
      GitHubAccessTokenResponse tokenResponse = gitHubService.getAccessToken(oauth2AuthorizationCode.getCode(),
          gitHubProperty);
      String accessToken = tokenResponse.getAccessToken();
      var githubUser = gitHubService.getAndUpdateUser(accessToken);
      return jwtService.generateToken(githubUser, accessToken);
    } catch (Oauth2ExchangeCodeException e) {
      log.error("Login Github failed: ", e);
      throw new Oauth2ExchangeCodeException(HttpStatus.BAD_REQUEST.name(), e.getErrorDescription());
    } catch (Exception e) {
      log.error("Error getting authentication token: ", e);
      throw new Oauth2ExchangeCodeException(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getMessage());
    }
  }

  @Override
  public String validateTokenAndGenerateJWT(String authorizationHeader) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    if (ObjectUtils.isEmpty(token)) {
      throw new Oauth2ExchangeCodeException(HttpStatus.BAD_REQUEST.name(), "Invalid Authorization header");
    }
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    return jwtService.generateJWTFromGitHubToken(token);
  }

}
