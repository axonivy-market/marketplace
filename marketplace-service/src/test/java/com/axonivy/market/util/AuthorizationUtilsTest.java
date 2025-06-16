package com.axonivy.market.util;

import com.axonivy.market.util.validator.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationUtilsTest {

  private AuthorizationUtils validator;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setup() throws Exception {
    validator = new AuthorizationUtils();
    context = mock(ConstraintValidatorContext.class);

    Field field = AuthorizationUtils.class.getDeclaredField("allowedUrls");
    field.setAccessible(true);
    field.set(validator, List.of("https://example.com", "https://myhost.com/download"));
  }

  @Test
  void testBearerTokenInvalidFormat() {
    String token = AuthorizationUtils.getBearerToken("Basic xyz123");
    assertNull(token);
  }

  @Test
  void testBearerTokenValid() {
    String token = AuthorizationUtils.getBearerToken("Bearer abc123");
    assertEquals("abc123", token);
  }

  @Test
  void testValidUrlAccepted() {
    String validUrl = "https://example.com/resource/file.txt";
    boolean result = validator.isValid(validUrl, context);
    assertTrue(result);
  }

  @Test
  void testUrlWithInvalidHost() {
    String invalidHostUrl = "http://localhost:8080/file";
    boolean result = validator.isValid(invalidHostUrl, context);
    assertFalse(result);
  }

  @Test
  void testUrlNotInAllowedList() {
    String notAllowed = "https://google.com/search";
    boolean result = validator.isValid(notAllowed, context);
    assertFalse(result);
  }

  @Test
  void testMalformedUrl() {
    String malformed = "http://::invalid_url";
    boolean result = validator.isValid(malformed, context);
    assertFalse(result);
  }

  @Test
  void testUrlWithoutHost() {
    String noHostUrl = "mailto:user@example.com";
    boolean result = validator.isValid(noHostUrl, context);
    assertFalse(result);
  }
}
