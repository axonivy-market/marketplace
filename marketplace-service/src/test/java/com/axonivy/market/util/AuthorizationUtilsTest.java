package com.axonivy.market.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationUtilsTest {
  @ParameterizedTest
  @ValueSource(strings = {
      "https://market.axonivy.com",
      "https://maven.axonivy.com"
  })
  void testIsAllowedUrl_ValidUrls(String url) {
    assertTrue(AuthorizationUtils.isAllowedUrl(url), "Expected the URL to be allowed: " + url);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "https://example.com"
  })
  void testIsAllowedUrl_InvalidUrls(String url) {
    assertFalse(AuthorizationUtils.isAllowedUrl(url), "Expected the URL to be blocked: " + url);
  }
}