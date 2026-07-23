package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Log4j2
@Component
@NoArgsConstructor
public class JwtServiceImpl implements JwtService {
  private static final int TOKEN_EXPIRE_DURATION = 86_400_000;
  private static final long ADMIN_TOKEN_LIFETIME = 1;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  @Override
  public String generateToken(GithubUser githubUser, String accessToken) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(GitHubConstants.NAME, githubUser.getName());
    claims.put(GitHubConstants.USERNAME, githubUser.getUsername());
    claims.put(GitHubConstants.ACCESS_TOKEN, accessToken);
    return createNewJWTCompactToken(githubUser.getId(), claims, expiration);
  }

  @Override
  public String generateJWTFromGitHubToken(String accessToken) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(GitHubConstants.ACCESS_TOKEN, accessToken);
    return createNewJWTCompactToken(GitHubConstants.ADMIN_SESSION_TOKEN, claims, ADMIN_TOKEN_LIFETIME);
  }

  @Override
  public boolean validateToken(String token) {
    try {
      getClaimsJws(token);
      return true;
    } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
             IllegalArgumentException e) {
      log.error("Error validating token: ", e);
      return false;
    }
  }

  @Override
  public Claims getClaimsFromToken(String token) {
    token = unifyJWTToken(token);
    return getClaimsJws(token).getPayload();
  }

  public Jws<Claims> getClaimsJws(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token);
  }

  @Override
  public String getRawAccessToken(String jwtToken) {
    var claims = getClaimsFromToken(jwtToken);
    return claims.get(GitHubConstants.ACCESS_TOKEN, String.class);
  }

  private String unifyJWTToken(String jwtToken) {
    var token = Strings.CS.removeStart(jwtToken, CommonConstants.BEARER);
    return StringUtils.trim(token);
  }

  private String createNewJWTCompactToken(String subject, Map<String, Object> claims, long expiration) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration * TOKEN_EXPIRE_DURATION))
        .signWith(getSigningKey())
        .compact();
  }

  private SecretKey getSigningKey() {
    try {
      byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
      byte[] derivedKey = MessageDigest.getInstance("SHA-512").digest(secretBytes);
      return Keys.hmacShaKeyFor(derivedKey);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-512 is not available", e);
    }
  }
}
