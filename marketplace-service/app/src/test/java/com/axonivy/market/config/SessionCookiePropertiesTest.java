package com.axonivy.market.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.Cookie.SameSite;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionCookiePropertiesTest {

  @Test
  void shouldConvertSameSiteToEnum() {
    SessionCookieProperties properties = new SessionCookieProperties();
    properties.setName("SESSION");
    properties.setSecure(true);
    properties.setHttpOnly(false);
    properties.setSameSite("lax");

    assertEquals("SESSION", properties.getName());
    assertEquals(true, properties.isSecure());
    assertEquals(false, properties.isHttpOnly());
    assertEquals(SameSite.LAX, properties.getSameSiteEnum());
  }
}
