package com.axonivy.market.service;

import com.axonivy.market.model.AdminLoginResponse;
import com.axonivy.market.model.Oauth2AuthorizationCode;

import java.io.IOException;

public interface OAuth2Service {

  String loginToGitHubAndGetJWT(Oauth2AuthorizationCode oauth2AuthorizationCode);

  String validateTokenAndGenerateJWT(String authorizationHeader);

  AdminLoginResponse validateTokenAndGenerateJWT2(String authorizationHeader);
}
