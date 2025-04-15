package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.GithubUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest extends BaseSetup {

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
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String token = jwtService.generateToken(githubUser, ACCESS_TOKEN);

    assertNotNull(token);
    assertFalse(token.isEmpty());

    Claims claims = jwtService.getClaimsFromToken(token);
    assertEquals("123", claims.getSubject());
    assertEquals("John Doe", claims.get("name"));
    assertEquals("johndoe", claims.get("username"));
  }

  @Test
  void testValidateToken() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String validToken = jwtService.generateToken(githubUser, ACCESS_TOKEN);
    assertTrue(jwtService.validateToken(validToken));

    String invalidToken = "invalid.token.here";
    assertFalse(jwtService.validateToken(invalidToken));
  }

  @Test
  void testGetClaimsFromToken() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String token = jwtService.generateToken(githubUser, ACCESS_TOKEN);

    Claims claims = jwtService.getClaimsFromToken(token);
    assertNotNull(claims);
    assertEquals("123", claims.getSubject());
    assertEquals("John Doe", claims.get("name"));
    assertEquals("johndoe", claims.get("username"));
  }

  @Test
  void testGetClaimsJws() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String token = jwtService.generateToken(githubUser, ACCESS_TOKEN);

    Jws<Claims> claimsJws = jwtService.getClaimsJws(token);
    assertNotNull(claimsJws);
    assertNotNull(claimsJws.getBody());
    assertEquals("123", claimsJws.getBody().getSubject());
  }
}

