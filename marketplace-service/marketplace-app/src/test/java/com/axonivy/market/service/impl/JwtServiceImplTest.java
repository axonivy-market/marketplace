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

    assertNotNull(token, "Generated token should not be null for a valid GitHub user");
    assertFalse(token.isEmpty(), "Generated token should not be empty");

    Claims claims = jwtService.getClaimsFromToken(token);
    assertEquals("123", claims.getSubject(), "JWT subject should match the GitHub user ID");
    assertEquals("John Doe", claims.get("name"), "JWT claim 'name' should match the GitHub user name");
    assertEquals("johndoe", claims.get("username"), "JWT claim 'username' should match the GitHub user username");
  }

  @Test
  void testValidateToken() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String validToken = jwtService.generateToken(githubUser, ACCESS_TOKEN);
    assertTrue(jwtService.validateToken(validToken),
        "A token generated for a valid GitHub user should be recognized as valid");

    String invalidToken = "invalid.token.here";
    assertFalse(jwtService.validateToken(invalidToken),
        "A malformed or random token should be recognized as invalid");
  }

  @Test
  void testGetClaimsFromToken() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String token = jwtService.generateToken(githubUser, ACCESS_TOKEN);

    Claims claims = jwtService.getClaimsFromToken(token);
    assertNotNull(claims, "Extracted claims should not be null for a valid token");
    assertEquals("123", claims.getSubject(),
        "JWT subject should match the GitHub user ID set in the token");
    assertEquals("John Doe", claims.get("name"),
        "JWT claim 'name' should match the GitHub user's name");
    assertEquals("johndoe", claims.get("username"),
        "JWT claim 'username' should match the GitHub user's username");
  }

  @Test
  void testGetClaimsJws() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("123");
    githubUser.setName("John Doe");
    githubUser.setUsername("johndoe");

    String token = jwtService.generateToken(githubUser, ACCESS_TOKEN);

    Jws<Claims> claimsJws = jwtService.getClaimsJws(token);
    assertNotNull(claimsJws, "getClaimsJws should return a non-null JWS object for a valid token");
    assertNotNull(claimsJws.getBody(), "JWS body (claims) should not be null for a valid token");
    assertEquals("123", claimsJws.getBody().getSubject(),
        "JWS subject should match the GitHub user ID encoded in the token");
  }
}

