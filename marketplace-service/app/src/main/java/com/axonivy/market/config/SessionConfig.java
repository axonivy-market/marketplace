package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig {
  @Bean
  public CookieSerializer cookieSerializer(
      @Value("${SESSION_COOKIE_NAME:ADMIN_SESSION}") String sessionCookieName,
      @Value("${SESSION_COOKIE_SECURE:true}") boolean secureCookie) {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName(sessionCookieName);
    serializer.setCookiePath("/");
    serializer.setUseHttpOnlyCookie(true);
    serializer.setUseSecureCookie(secureCookie);
    serializer.setSameSite("Lax");
    serializer.setUseBase64Encoding(false);
    return serializer;
  }
}
