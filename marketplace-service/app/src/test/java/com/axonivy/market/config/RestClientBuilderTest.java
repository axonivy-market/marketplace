package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestClientBuilderTest {

  @Mock
  private AppSettingService appSettingService;

  private RestClientBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new RestClientBuilder(appSettingService);
  }

  @Test
  void testBuildCreatesClientWithConfiguredTimeout() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(5000L);

    RestClient client = builder.build();

    assertNotNull(client, "RestClient should not be null");
  }

  @Test
  void testBuildReturnsCachedClientWhenTimeoutUnchanged() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(10000L);

    RestClient first = builder.build();
    RestClient second = builder.build();

    assertSame(first, second, "Should return cached client when timeout has not changed");
    verify(appSettingService, times(2)).getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);
  }

  @Test
  void testBuildCreatesNewClientWhenTimeoutChanges() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(5000L);
    RestClient first = builder.build();

    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(15000L);
    RestClient second = builder.build();

    assertNotSame(first, second, "Should create new client when timeout changes");
  }

  @Test
  void testBuildWithZeroTimeout() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(0L);

    RestClient client = builder.build();

    assertNotNull(client, "RestClient should not be null even with zero timeout");
  }
}
