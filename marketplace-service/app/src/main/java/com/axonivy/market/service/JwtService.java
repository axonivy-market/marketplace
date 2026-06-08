package com.axonivy.market.service;

import com.axonivy.market.model.UserInfo;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtService {

  String generateToken(UserInfo userInfo);

  boolean validateToken(String token);

  Jwt decodeToken(String token);
}
