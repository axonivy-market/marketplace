package com.axonivy.market.config;

import static com.axonivy.market.constants.RequestMappingConstants.USER_MAPPING;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final HeaderInterceptor headerInterceptor;

  public WebConfig(HeaderInterceptor headerInterceptor) {
    this.headerInterceptor = headerInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(headerInterceptor).addPathPatterns(USER_MAPPING);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowedHeaders("Accept-Language", "Content-Type", "X-Requested-By", "x-requested-with", "X-Forwarded-Host")
        .maxAge(3600);
  }
}
