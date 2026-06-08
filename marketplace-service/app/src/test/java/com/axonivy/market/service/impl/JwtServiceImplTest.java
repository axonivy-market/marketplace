package com.axonivy.market.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.config.SecurityConfig;
import com.axonivy.market.security.SecurityAuthorities;
import com.axonivy.market.security.SecurityJwtProperties;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceImplTest extends BaseSetup {

  private static final String SECRET = "12345678901234567890123456789012";

  private JwtServiceImpl jwtService;
  private SecurityJwtProperties securityJwtProperties;

  @BeforeEach
  void setUp() {
    securityJwtProperties = new SecurityJwtProperties();
    securityJwtProperties.setSecret(SECRET);
    securityJwtProperties.setExpirationMinutes(120);
    securityJwtProperties.setIssuer("marketplace-service-test");
    securityJwtProperties.setAudience("marketplace-admin-api-test");
    securityJwtProperties.validateSecretStrength();

    var securityConfig = new SecurityConfig();
    SecretKey signingKey = securityConfig.securityJwtSigningKey(securityJwtProperties);
    jwtService = new JwtServiceImpl(
        securityConfig.jwtEncoder(signingKey),
        securityConfig.jwtDecoder(signingKey, securityJwtProperties),
        securityJwtProperties);
  }

  @Test
  void testGenerateToken() {
    String token = jwtService.generateToken(getMockUserInfo());

    assertNotNull(token);
    assertFalse(token.isBlank());

    var jwt = jwtService.decodeToken(token);
    assertEquals("github-user-id", jwt.getSubject());
    assertEquals("mockUser", jwt.getClaimAsString("name"));
    assertEquals("mockUser", jwt.getClaimAsString(JwtServiceImpl.USERNAME_CLAIM));
    assertEquals("GitHub", jwt.getClaimAsString(JwtServiceImpl.PROVIDER_CLAIM));
    assertTrue(Boolean.TRUE.equals(jwt.getClaim(JwtServiceImpl.ADMIN_CLAIM)));
    assertTrue(jwt.getClaimAsStringList(JwtServiceImpl.AUTHORITIES_CLAIM).contains(SecurityAuthorities.MARKET_ADMIN));
    assertEquals("marketplace-service-test", jwt.getClaimAsString("iss"));
    assertTrue(jwt.getAudience().contains("marketplace-admin-api-test"));
    assertNotNull(jwt.getId());
  }

  @Test
  void testValidateToken() {
    String validToken = jwtService.generateToken(getMockUserInfo());

    assertTrue(jwtService.validateToken(validToken));
    assertFalse(jwtService.validateToken("invalid.token.here"));
  }

  @Test
  void testRejectTokenWithUnexpectedAudience() {
    String token = jwtService.generateToken(getMockUserInfo());

    var mismatchedProperties = new SecurityJwtProperties();
    mismatchedProperties.setSecret(SECRET);
    mismatchedProperties.setExpirationMinutes(120);
    mismatchedProperties.setIssuer("marketplace-service-test");
    mismatchedProperties.setAudience("different-audience");
    mismatchedProperties.validateSecretStrength();

    var securityConfig = new SecurityConfig();
    SecretKey signingKey = securityConfig.securityJwtSigningKey(mismatchedProperties);
    var strictService = new JwtServiceImpl(
        securityConfig.jwtEncoder(signingKey),
        securityConfig.jwtDecoder(signingKey, mismatchedProperties),
        mismatchedProperties);

    assertFalse(strictService.validateToken(token));
  }

  @Test
  void testExpirationMinutesApplied() {
    String token = jwtService.generateToken(getMockUserInfo());

    var jwt = jwtService.decodeToken(token);
    long lifetimeSeconds = jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond();
    assertEquals(120L * 60L, lifetimeSeconds);
  }
}
