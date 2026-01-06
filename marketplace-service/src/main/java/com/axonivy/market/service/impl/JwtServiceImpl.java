package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.service.JwtService;
import com.axonivy.market.util.validator.AuthorizationUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@NoArgsConstructor
public class JwtServiceImpl implements JwtService {
  private static final int TOKEN_EXPIRE_DURATION = 86_400_000;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  public String generateToken(GithubUser githubUser, String accessToken) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(CommonConstants.NAME, githubUser.getName());
    claims.put(CommonConstants.USERNAME, githubUser.getUsername());
    claims.put(CommonConstants.ACCESS_TOKEN, accessToken);
    return createNewJWTCompactToken(githubUser.getId(), claims);
  }

  @Override
  public String generateJWTFromGitHubToken(String accessToken) {
    return createNewJWTCompactToken(CommonConstants.ADMIN_SESSION_TOKEN, Map.of(CommonConstants.ACCESS_TOKEN, accessToken));
  }

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

  public Claims getClaimsFromToken(String token) {
    return getClaimsJws(token).getBody();
  }

  public Jws<Claims> getClaimsJws(String token) {
    return Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
  }

  @Override
  public String getRawAccessToken(String jwtToken) {
    var token = AuthorizationUtils.getBearerToken(jwtToken);
    Claims claims = getClaimsFromToken(token);
    return claims.get(CommonConstants.ACCESS_TOKEN, String.class);
  }

  private String createNewJWTCompactToken(String subject, Map<String, Object> claims) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration * TOKEN_EXPIRE_DURATION))
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact();
  }
}