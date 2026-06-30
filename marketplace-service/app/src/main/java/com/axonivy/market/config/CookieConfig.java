package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public class CookieConfig {
  @Bean
  public CookieSameSiteSupplier applicationCookieSameSiteSupplier(
      @Value("${server.servlet.session.cookie.same-site:None}") String sameSite) {
    return CookieSameSiteSupplier.of(SameSite.valueOf(sameSite.toUpperCase(Locale.ROOT)));
  }

}
