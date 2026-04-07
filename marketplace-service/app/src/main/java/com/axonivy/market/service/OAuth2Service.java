package com.axonivy.market.service;

import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.model.UserInfo;

public interface OAuth2Service {

  String loginToGitHubAndGetJWT(Oauth2AuthorizationCode oauth2AuthorizationCode);

  UserInfo validateTokenAndGenerateJWT(String authorizationHeader);
}
