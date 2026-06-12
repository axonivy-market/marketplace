package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class CookieConfig {
  @Bean
  public CookieSameSiteSupplier applicationCookieSameSiteSupplier(
      @Value("${SESSION_COOKIE_SAME_SITE:Lax}") String sameSite) {
    return CookieSameSiteSupplier.of(SameSite.valueOf(sameSite.toUpperCase(Locale.ROOT)));
  }
}
