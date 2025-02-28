package com.axonivy.market.service;

import com.axonivy.market.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtService {
  String generateToken(User user, String accessToken);

  boolean validateToken(String token);

  Claims getClaimsFromToken(String token);
}
