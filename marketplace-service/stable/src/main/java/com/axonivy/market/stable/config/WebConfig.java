package com.axonivy.market.stable.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.axonivy.market.core.constants.CoreCommonConstants;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${market.cors.allowed.mappings}")
  private String marketCorsMappings;

  @Value("${market.cors.allowed.methods}")
  private String marketCorsMethods;

  @Value("${market.cors.allowed.origin.patterns}")
  private String marketCorsAllowedOriginPatterns;

  @Value("${market.cors.allowed.origin.maxAge}")
  private int marketCorsAllowedOriginMaxAge;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(marketCorsMappings)
        .allowedOriginPatterns(marketCorsAllowedOriginPatterns)
        .allowedMethods(marketCorsMethods.split(CoreCommonConstants.COMMA))
        .allowedHeaders("*")
        .maxAge(marketCorsAllowedOriginMaxAge);
  }
}
