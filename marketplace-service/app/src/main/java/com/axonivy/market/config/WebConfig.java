package com.axonivy.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.axonivy.market.core.config.BaseCorsWebConfig;

@Configuration
public class WebConfig extends BaseCorsWebConfig {

  private static final String[] EXCLUDE_PATHS = {"/", "/swagger-ui/**", "/api-docs/**", "/api/product-details/**/json",
      "/api/image/**"};
  private static final String[] ALLOWED_HEADERS = {"Accept-Language", "Content-Type", "Authorization",
      "X-Requested-By", "x-requested-with", "X-Forwarded-Host", "x-xsrf-token", "x-authorization"};

  private final MarketHeaderInterceptor headerInterceptor;
  private final AsyncTaskExecutor sharedVirtualThreadExecutor;

  public WebConfig(MarketHeaderInterceptor headerInterceptor, AsyncTaskExecutor sharedVirtualThreadExecutor) {
    this.headerInterceptor = headerInterceptor;
    this.sharedVirtualThreadExecutor = sharedVirtualThreadExecutor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(headerInterceptor).excludePathPatterns(EXCLUDE_PATHS);
  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setTaskExecutor(sharedVirtualThreadExecutor);
  }

  @Override
  protected String[] resolveAllowedHeaders() {
    return ALLOWED_HEADERS;
  }
}
