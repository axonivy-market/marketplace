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

  private static final long DEFAULT_TIMEOUT_MILLIS = 10_000L;
  private final AppSettingService appSettingService;
  private OkHttpClient cachedClient;
  private long cachedTimeoutMillis = -1L;

  public synchronized OkHttpClient build() {
    long timeoutMillis = DEFAULT_TIMEOUT_MILLIS;

    try {
      String configured = appSettingService.getValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);
      if (StringUtils.isNotBlank(configured)) {
        timeoutMillis = Long.parseLong(configured);
      }
    } catch (NumberFormatException ignored) {
        // Use default if parsing fails
    }

    if (cachedClient != null && timeoutMillis == cachedTimeoutMillis) {
      return cachedClient;
    }

    cachedClient = new OkHttpClient.Builder().callTimeout(Duration.ofMillis(timeoutMillis)).build();
    cachedTimeoutMillis = timeoutMillis;
    return cachedClient;
  }
}

