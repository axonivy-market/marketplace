package com.axonivy.market.config;

import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConfig {
  @Bean
  public CookieSameSiteSupplier applicationCookieSameSiteSupplier(SessionCookieProperties cookieProperties) {
    return CookieSameSiteSupplier.of(cookieProperties.getSameSiteEnum());
  }

}
