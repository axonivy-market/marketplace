package com.axonivy.market.service;

import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.model.UserInfo;

public interface OAuth2Service {
  
  /**
   * <p>
   * Login to GitHub and get JWT
   * </p>
   *
   * @param  oauth2AuthorizationCode
   *              type {@link Oauth2AuthorizationCode}
   * @return {@link String}
   * @author nqhoan
   */
  String loginToGitHubAndGetJWT(Oauth2AuthorizationCode oauth2AuthorizationCode);

  /**
   * <p>
   * Validate token and generate JWT by authorization header
   * </p>
   *
   * @param  authorizationHeader
   *              type {@link String}
   * @return {@link UserInfo}
   * @author vhhoang
   */
  UserInfo validateTokenAndGenerateJWT(String authorizationHeader);
}
