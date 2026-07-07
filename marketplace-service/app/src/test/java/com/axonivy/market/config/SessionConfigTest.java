package com.axonivy.market.config;

import org.junit.jupiter.api.Test;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionConfigTest {

  private final SessionConfig sessionConfig = new SessionConfig();

  @Test
  void cookieSerializerShouldUseSessionCookieProperties() {
    SessionCookieProperties properties = new SessionCookieProperties();
    properties.setName("MY_SESSION");
    properties.setSecure(true);
    properties.setHttpOnly(false);
    properties.setSameSite("strict");

    CookieSerializer cookieSerializer = sessionConfig.cookieSerializer(properties);

    assertInstanceOf(DefaultCookieSerializer.class, cookieSerializer);
    DefaultCookieSerializer serializer = (DefaultCookieSerializer) cookieSerializer;
    assertEquals("MY_SESSION", ReflectionTestUtils.getField(serializer, "cookieName"));
    assertEquals(true, ReflectionTestUtils.getField(serializer, "useSecureCookie"));
    assertEquals(false, ReflectionTestUtils.getField(serializer, "useHttpOnlyCookie"));
    assertEquals("/", ReflectionTestUtils.getField(serializer, "cookiePath"));
    assertEquals("strict", ReflectionTestUtils.getField(serializer, "sameSite"));
    assertFalse((Boolean) ReflectionTestUtils.getField(serializer, "useBase64Encoding"));
    assertTrue(serializer.getRememberMeRequestAttribute() == null);
  }
}
