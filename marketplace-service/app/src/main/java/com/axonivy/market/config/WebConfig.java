package com.axonivy.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.axonivy.market.core.config.BaseCorsWebConfig;

@Configuration
public class WebConfig extends BaseCorsWebConfig {

  private static final String[] EXCLUDE_PATHS = {"/", "/swagger-ui/**", "/api-docs/**", "/api/product-details/**/json",
      "/api/image/**"};
  private static final String[] ALLOWED_HEADERS = {"Accept-Language", "Content-Type", "Authorization",
      "X-Requested-By", "x-requested-with", "X-Forwarded-Host", "x-xsrf-token", "x-authorization"};

  private final MarketHeaderInterceptor headerInterceptor;

  public WebConfig(MarketHeaderInterceptor headerInterceptor) {
    this.headerInterceptor = headerInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(headerInterceptor).excludePathPatterns(EXCLUDE_PATHS);
  }

  @Override
  protected String[] resolveAllowedHeaders() {
    return ALLOWED_HEADERS;
  }
}
