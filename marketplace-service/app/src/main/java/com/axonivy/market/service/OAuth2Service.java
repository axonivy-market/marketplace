package com.axonivy.market.service;

import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.model.UserInfo;

public interface OAuth2Service {
  
  /**
   * <p>
   * Authenticates a user with GitHub using OAuth2 authorization code from the login flow. Exchanges the
   * authorization code for a GitHub access token, retrieves user information, creates or updates the user
   * record, and returns a signed JWT token for session management.
   * </p>
   *
   * @param  oauth2AuthorizationCode
   *              type {@link Oauth2AuthorizationCode} - request object containing the OAuth2 authorization code
   *              received from GitHub after user consent
   * @return {@link String} - the signed JWT token for authenticated session; returns null if GitHub authentication fails
   * @author nqhoan
   */
  String loginToGitHubAndGetJWT(Oauth2AuthorizationCode oauth2AuthorizationCode);

  /**
   * <p>
   * Validates an authorization header containing a GitHub access token and generates a new JWT token
   * for session management. Extracts and validates the token, retrieves user information from GitHub,
   * and returns user details along with the generated JWT.
   * </p>
   *
   * @param  authorizationHeader
   *              type {@link String} - the Authorization header value (format: "Bearer &lt;github_token&gt;")
   * @return {@link UserInfo} - user information object containing user details and generated JWT token;
   *         throws exception if token is invalid or GitHub authentication fails
   * @author vhhoang
   */
  UserInfo validateTokenAndGenerateJWT(String authorizationHeader);
}
