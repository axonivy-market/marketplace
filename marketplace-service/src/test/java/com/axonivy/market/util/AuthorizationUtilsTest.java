package com.axonivy.market.util;

import com.axonivy.market.util.validator.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationUtilsTest {

//  private ConstraintValidatorContext context;
//
//  @BeforeEach
//  void setup() throws Exception {
//    context = mock(ConstraintValidatorContext.class);
//  }

  @Test
  void testBearerTokenInvalidFormat() {
    String token = AuthorizationUtils.getBearerToken("Basic xyz123");
    assertNull(token, "Expected null for non-Bearer token format, but got: " + token);
  }

  @Test
  void testBearerTokenValid() {
    String token = AuthorizationUtils.getBearerToken("Bearer abc123");
    assertEquals("abc123", token, "Expected to extract 'abc123' from Bearer token");
  }
}
