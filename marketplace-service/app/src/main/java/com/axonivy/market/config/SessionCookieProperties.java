package com.axonivy.market.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Cookie.SameSite;

import java.util.Locale;

@Getter
@Setter
@ConfigurationProperties(prefix = "server.servlet.session.cookie")
public class SessionCookieProperties {
  private String name;
  private boolean secure;
  private boolean httpOnly;
  private String sameSite;

  public SameSite getSameSiteEnum() {
    return SameSite.valueOf(sameSite.toUpperCase(Locale.ROOT));
  }
}
