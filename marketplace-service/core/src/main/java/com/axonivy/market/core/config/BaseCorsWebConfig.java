package com.axonivy.market.core.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axonivy.market.core.constants.CoreCommonConstants;

public abstract class BaseCorsWebConfig implements WebMvcConfigurer {

  private static final String DEFAULT_MAPPING = "/**";
  private static final String DEFAULT_METHODS = "GET,POST,PUT,DELETE,OPTIONS";
  private static final String DEFAULT_ALLOWED_ORIGIN_PATTERNS = "*";
  private static final int DEFAULT_MAX_AGE = 3600;

  @Value("${market.cors.allowed.mappings:" + DEFAULT_MAPPING + "}")
  private String marketCorsMappings;

  @Value("${market.cors.allowed.methods:" + DEFAULT_METHODS + "}")
  private String marketCorsMethods;

  @Value("${market.cors.allowed.origin.patterns:" + DEFAULT_ALLOWED_ORIGIN_PATTERNS + "}")
  private String marketCorsAllowedOriginPatterns;

  @Value("${market.cors.allowed.origin.maxAge:" + DEFAULT_MAX_AGE + "}")
  private int marketCorsAllowedOriginMaxAge;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(marketCorsMappings)
        .allowedOriginPatterns(marketCorsAllowedOriginPatterns)
        .allowedMethods(resolveAllowedMethods())
        .allowedHeaders(resolveAllowedHeaders())
        .allowCredentials(true)
        .maxAge(marketCorsAllowedOriginMaxAge);
  }

  protected String[] resolveAllowedHeaders() {
    return new String[] {"*"};
  }

  protected String[] resolveAllowedMethods() {
    return Arrays.stream(marketCorsMethods.split(CoreCommonConstants.COMMA))
        .map(String::trim)
        .filter(method -> !method.isEmpty())
        .toArray(String[]::new);
  }
}