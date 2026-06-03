package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OkHttpClientBuilder {

  private final AppSettingService appSettingService;
  private volatile OkHttpClient cachedClient;
  private volatile long cachedTimeoutMillis = -1L;

  public OkHttpClient build() {
    long timeoutMillis = 10000L;
    try {
      String configured = appSettingService.getValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);
      if (StringUtils.isNotBlank(configured)) {
        timeoutMillis = Long.parseLong(configured);
      }
    } catch (Exception ignored) {
    }

    // return cached client if timeout hasn't changed
    OkHttpClient client = cachedClient;
    if (client != null && timeoutMillis == cachedTimeoutMillis) {
      return client;
    }

    synchronized (this) {
      if (cachedClient != null && timeoutMillis == cachedTimeoutMillis) {
        return cachedClient;
      }
      OkHttpClient newClient = new OkHttpClient.Builder()
          .callTimeout(Duration.ofMillis(timeoutMillis))
          .build();
      cachedClient = newClient;
      cachedTimeoutMillis = timeoutMillis;
      return newClient;
    }
  }
}

