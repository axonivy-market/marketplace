package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtServiceImpl implements JwtService {
  private static final int TOKEN_EXPIRE_DURATION = 86400000;

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
    } catch (Exception e) {
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
