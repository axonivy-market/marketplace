package com.axonivy.market.service.impl;

import com.axonivy.market.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

  private static final String SECRET = "mySecret";
  private static final long EXPIRATION = 7L; // 7 days

  @InjectMocks
  private JwtServiceImpl jwtService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
  }

  @Test
  void testGenerateToken() {
    User user = new User();
    user.setId("123");
    user.setName("John Doe");
    user.setUsername("johndoe");

    String token = jwtService.generateToken(user);

    assertNotNull(token);
    assertFalse(token.isEmpty());

    Claims claims = jwtService.getClaimsFromToken(token);
    assertEquals("123", claims.getSubject());
    assertEquals("John Doe", claims.get("name"));
    assertEquals("johndoe", claims.get("username"));
  }

  @Test
  void testValidateToken() {
    User user = new User();
    user.setId("123");
    user.setName("John Doe");
    user.setUsername("johndoe");

    String validToken = jwtService.generateToken(user);
    assertTrue(jwtService.validateToken(validToken));

    String invalidToken = "invalid.token.here";
    assertFalse(jwtService.validateToken(invalidToken));
  }

  @Test
  void testGetClaimsFromToken() {
    User user = new User();
    user.setId("123");
    user.setName("John Doe");
    user.setUsername("johndoe");

    String token = jwtService.generateToken(user);

    Claims claims = jwtService.getClaimsFromToken(token);
    assertNotNull(claims);
    assertEquals("123", claims.getSubject());
    assertEquals("John Doe", claims.get("name"));
    assertEquals("johndoe", claims.get("username"));
  }

  @Test
  void testGetClaimsJws() {
    User user = new User();
    user.setId("123");
    user.setName("John Doe");
    user.setUsername("johndoe");

    String token = jwtService.generateToken(user);

    Jws<Claims> claimsJws = jwtService.getClaimsJws(token);
    assertNotNull(claimsJws);
    assertNotNull(claimsJws.getBody());
    assertEquals("123", claimsJws.getBody().getSubject());
  }
}

