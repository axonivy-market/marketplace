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
      @Value("${server.servlet.session.cookie.name:ADMIN_SESSION}") String sessionCookieName,
      @Value("${server.servlet.session.cookie.secure:true}") boolean secureCookie,
      @Value("${server.servlet.session.cookie.http-only:true}") boolean httpOnlyCookie,
      @Value("${server.servlet.session.cookie.same-site:Lax}") String sameSite) {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName(sessionCookieName);
    serializer.setCookiePath("/");
    serializer.setUseHttpOnlyCookie(httpOnlyCookie);
    serializer.setUseSecureCookie(secureCookie);
    serializer.setSameSite(sameSite);
    serializer.setUseBase64Encoding(false);
    return serializer;
  }
}
