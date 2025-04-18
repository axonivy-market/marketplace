package com.axonivy.market.service;

import com.axonivy.market.entity.GithubUser;
import io.jsonwebtoken.Claims;

public interface JwtService {
  String generateToken(GithubUser githubUser, String accessToken);

  boolean validateToken(String token);

  Claims getClaimsFromToken(String token);
}
