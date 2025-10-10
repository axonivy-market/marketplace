package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.service.JwtService;
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
    claims.put("name", githubUser.getName());
    claims.put("username", githubUser.getUsername());
    claims.put("accessToken", accessToken);
    return Jwts.builder().setClaims(claims).setSubject(githubUser.getId()).setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration * TOKEN_EXPIRE_DURATION))
        .signWith(SignatureAlgorithm.HS512, secret).compact();
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
}
