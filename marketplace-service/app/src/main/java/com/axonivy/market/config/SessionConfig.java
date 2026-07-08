package com.axonivy.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig {
  @Bean
  public CookieSerializer cookieSerializer(SessionCookieProperties cookieProperties) {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName(cookieProperties.getName());
    serializer.setCookiePath("/");
    serializer.setUseHttpOnlyCookie(cookieProperties.isHttpOnly());
    serializer.setUseSecureCookie(cookieProperties.isSecure());
    serializer.setSameSite(cookieProperties.getSameSite());
    serializer.setUseBase64Encoding(false);
    return serializer;
  }
}
