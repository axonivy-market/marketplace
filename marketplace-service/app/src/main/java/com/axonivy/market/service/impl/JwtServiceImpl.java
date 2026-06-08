package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.security.SecurityAuthorities;
import com.axonivy.market.security.SecurityJwtProperties;
import com.axonivy.market.service.JwtService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

  public static final String USERNAME_CLAIM = "username";
  public static final String AVATAR_URL_CLAIM = "avatarUrl";
  public static final String PROVIDER_CLAIM = "provider";
  public static final String AUTHORITIES_CLAIM = "authorities";
  public static final String ADMIN_CLAIM = "admin";

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final SecurityJwtProperties securityJwtProperties;

  @Override
  public String generateToken(UserInfo userInfo) {
    var now = java.time.Instant.now();
    var claims = JwtClaimsSet.builder()
        .issuer(securityJwtProperties.getIssuer())
        .issuedAt(now)
        .expiresAt(now.plusSeconds(securityJwtProperties.getExpirationMinutes() * 60L))
        .audience(List.of(securityJwtProperties.getAudience()))
        .id(UUID.randomUUID().toString())
        .subject(userInfo.getGitHubId())
        .claim(GitHubConstants.NAME, userInfo.getName())
        .claim(USERNAME_CLAIM, userInfo.getUsername())
        .claim(AVATAR_URL_CLAIM, userInfo.getAvatarUrl())
        .claim(PROVIDER_CLAIM, userInfo.getProvider())
        .claim(ADMIN_CLAIM, true)
        .claim(AUTHORITIES_CLAIM, List.of(SecurityAuthorities.MARKET_ADMIN))
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS256).build(),
        claims)).getTokenValue();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      decodeToken(token);
      return true;
    } catch (JwtException ex) {
      return false;
    }
  }

  @Override
  public Jwt decodeToken(String token) {
    return jwtDecoder.decode(unifyToken(token));
  }

  private String unifyToken(String token) {
    return StringUtils.trim(StringUtils.removeStart(token, CommonConstants.BEARER));
  }
}
