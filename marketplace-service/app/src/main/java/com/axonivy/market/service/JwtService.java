package com.axonivy.market.service;

import com.axonivy.market.entity.GithubUser;
import io.jsonwebtoken.Claims;

public interface JwtService {

  /**
   * <p>
   * Generate JWT token
   * </p>
   *
   * @param  githubUser
   *              type {@link GithubUser}
   * @param  accessToken
   *              type {@link String}
   * @return {@link String}
   * @author tvtphuc
   */
  String generateToken(GithubUser githubUser, String accessToken);

  /**
   * <p>
   * Generate JWT token by GitHub token
   * </p>
   *
   * @param  accessToken
   *              type {@link String}
   * @return {@link String}
   * @author nqhoan
   */
  String generateJWTFromGitHubToken(String accessToken);

  /**
   * <p>
   * Validate token
   * </p>
   *
   * @param  token
   *              type {@link String}
   * @return {@link boolean}
   * @author ndkhanh
   */
  boolean validateToken(String token);

  /**
   * <p>
   * Get claims from token
   * </p>
   *
   * @param  token
   *              type {@link String}
   * @return {@link Claims}
   * @author ndkhanh
   */
  Claims getClaimsFromToken(String token);

  /**
   * <p>
   * Get raw access token
   * </p>
   *
   * @param  jwtToken
   *              type {@link String}
   * @return {@link String}
   * @author nqhoan
   */
  String getRawAccessToken(String jwtToken);
}
