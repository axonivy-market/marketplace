package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OkHttpClientBuilder {

  private final AppSettingService appSettingService;
  private OkHttpClient cachedClient;
  private long cachedTimeoutMillis = -1L;

  public synchronized OkHttpClient build() {
    long timeoutMillis = appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);
    if (cachedClient != null && timeoutMillis == cachedTimeoutMillis) {
      return cachedClient;
    }

    cachedClient = new OkHttpClient.Builder().callTimeout(Duration.ofMillis(timeoutMillis)).build();
    cachedTimeoutMillis = timeoutMillis;
    return cachedClient;
  }
}

