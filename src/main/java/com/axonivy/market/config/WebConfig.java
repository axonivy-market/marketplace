package com.axonivy.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String[] EXCLUDE_PATHS = { "/swagger-ui/**", "/api-docs/**" };
  private static final String[] ALLOWED_HEADERS = { "Accept-Language", "Content-Type", "X-Requested-By",
      "x-requested-with", "X-Forwarded-Host" };
  private static final String[] ALLOWED_METHODS = { "GET", "POST", "PUT", "DELETE" };

  private final MarketHeaderInterceptor headerInterceptor;

  public WebConfig(MarketHeaderInterceptor headerInterceptor) {
    this.headerInterceptor = headerInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(headerInterceptor).excludePathPatterns(EXCLUDE_PATHS);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods(ALLOWED_METHODS)
        .allowedHeaders(ALLOWED_HEADERS)
        .maxAge(3600);
  }
}
