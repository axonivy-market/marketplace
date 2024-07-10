package com.axonivy.market.service.impl;

import com.axonivy.market.entity.User;
import com.axonivy.market.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtServiceImpl implements JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("name", user.getName());
    claims.put("username", user.getUsername());
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(user.getId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 86400000))
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact();
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

  private Jws<Claims> getClaimsJws(String token) {
    return Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
  }
}