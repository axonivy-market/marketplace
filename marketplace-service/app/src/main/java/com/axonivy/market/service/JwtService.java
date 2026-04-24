package com.axonivy.market.service;

import com.axonivy.market.entity.GithubUser;
import io.jsonwebtoken.Claims;

public interface JwtService {

  /**
   * <p>
   * Generates a JWT token for a GitHub user with embedded access token. Creates a signed JSON Web Token
   * containing user information and the original GitHub OAuth2 access token for authenticated API access.
   * </p>
   *
   * @param  githubUser
   *              type {@link GithubUser} - the GitHub user object containing profile information
   * @param  accessToken
   *              type {@link String} - the GitHub OAuth2 access token to embed in the JWT
   * @return {@link String} - the generated JWT token string; returns null if generation fails
   * @author tvtphuc
   */
  String generateToken(GithubUser githubUser, String accessToken);

  /**
   * <p>
   * Generates a JWT token directly from a GitHub access token. Creates a signed token using the provided access token.
   * </p>
   *
   * @param  accessToken
   *              type {@link String} - the GitHub OAuth2 access token to use for token generation
   * @return {@link String} - the generated JWT token string; returns null if generation fails or token is invalid
   * @author nqhoan
   */
  String generateJWTFromGitHubToken(String accessToken);

  /**
   * <p>
   * Validates a JWT token for authenticity and expiration. Checks the token signature, issuer, and
   * expiration time to ensure the token is valid and can be trusted for authentication.
   * </p>
   *
   * @param  token
   *              type {@link String} - the JWT token string to validate
   * @return {@link boolean} - true if the token is valid and not expired; false if invalid or expired
   * @author ndkhanh
   */
  boolean validateToken(String token);

  /**
   * <p>
   * Extracts all claims from a JWT token. Parses the token and returns the payload containing user
   * information, expiration time, and other embedded data.
   * </p>
   *
   * @param  token
   *              type {@link String} - the JWT token string to extract claims from
   * @return {@link Claims} - the JWT claims object containing all token payload data;
   * @author ndkhanh
   */
  Claims getClaimsFromToken(String token);

  /**
   * <p>
   * Extracts the original GitHub access token embedded in a JWT token. Retrieves the raw GitHub OAuth2
   * token that was stored during JWT generation, allowing downstream services to make authenticated GitHub
   * API calls on behalf of the user.
   * </p>
   *
   * @param  jwtToken
   *              type {@link String} - the JWT token string containing the embedded GitHub access token
   * @return {@link String} - the raw GitHub access token; returns null if JWT is invalid or token not found
   * @author nqhoan
   */
  String getRawAccessToken(String jwtToken);
}
