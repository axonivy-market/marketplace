package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OkHttpClientBuilderTest {

  @Mock
  private AppSettingService appSettingService;

  private OkHttpClientBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new OkHttpClientBuilder(appSettingService);
  }

  @Test
  void testBuildCreatesClientWithConfiguredTimeout() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(5000L);

    OkHttpClient client = builder.build();

    assertNotNull(client, "OkHttpClient should not be null");
    assertEquals(5000, client.callTimeoutMillis(), "Call timeout should match configured value");
  }

  @Test
  void testBuildReturnsCachedClientWhenTimeoutUnchanged() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(10000L);

    OkHttpClient first = builder.build();
    OkHttpClient second = builder.build();

    assertSame(first, second, "Should return cached client when timeout has not changed");
    verify(appSettingService, times(2)).getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);
  }

  @Test
  void testBuildCreatesNewClientWhenTimeoutChanges() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(5000L);
    OkHttpClient first = builder.build();

    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(15000L);
    OkHttpClient second = builder.build();

    assertNotSame(first, second, "Should create new client when timeout changes");
    assertEquals(15000, second.callTimeoutMillis(), "New client should have updated timeout");
  }

  @Test
  void testBuildWithZeroTimeout() {
    when(appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT)).thenReturn(0L);

    OkHttpClient client = builder.build();

    assertNotNull(client, "OkHttpClient should not be null even with zero timeout");
    assertEquals(0, client.callTimeoutMillis(), "Call timeout should be zero");
  }
}

