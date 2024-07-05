package com.axonivy.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String[] EXCLUDE_PATHS = { "/", "/swagger-ui/**", "/api-docs/**" };
  private static final String[] ALLOWED_HEADERS = { "Accept-Language", "Content-Type", "Authorization",
      "X-Requested-By", "x-requested-with", "X-Forwarded-Host" };
  private static final String[] ALLOWED_METHODS = { "GET", "POST", "PUT", "DELETE", "OPTIONS" };

  private final MarketHeaderInterceptor headerInterceptor;

  @Value("${market.cors.allowed.origin.patterns}")
  private String marketCorsAllowedOriginPatterns;

  @Value("${market.cors.allowed.origin.maxAge}")
  private int marketCorsAllowedOriginMaxAge;

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
        .allowedOriginPatterns(marketCorsAllowedOriginPatterns)
        .allowedMethods(ALLOWED_METHODS)
        .allowedHeaders(ALLOWED_HEADERS)
        .maxAge(marketCorsAllowedOriginMaxAge);
  }
}