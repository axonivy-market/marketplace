package com.axonivy.market.config;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

  @Mock
  private MarketHeaderInterceptor headerInterceptor;

  @Mock
  private AsyncTaskExecutor sharedVirtualThreadExecutor;

  @Mock
  private AsyncSupportConfigurer asyncSupportConfigurer;

  private WebConfig webConfig;

  @BeforeEach
  void setUp() {
    webConfig = new WebConfig(headerInterceptor, sharedVirtualThreadExecutor);
  }

  @Test
  void shouldUseSharedExecutorForMvcAsyncRequests() {
    webConfig.configureAsyncSupport(asyncSupportConfigurer);

    verify(asyncSupportConfigurer).setTaskExecutor(sharedVirtualThreadExecutor);
  }
}
