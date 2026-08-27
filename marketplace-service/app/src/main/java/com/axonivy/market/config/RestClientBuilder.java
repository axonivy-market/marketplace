package com.axonivy.market.config;

import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Builds and caches a {@link RestClient} configured with the connect/read timeout from
 * {@link AppSettingKey#GITHUB_CONNECT_TIMEOUT}. The cached client is rebuilt only when the
 * configured timeout value changes.
 */
@Component
@RequiredArgsConstructor
public class RestClientBuilder {

  private final AppSettingService appSettingService;
  private RestClient cachedClient;
  private long cachedTimeoutMillis = -1L;

  public synchronized RestClient build() {
    long timeoutMillis = appSettingService.getLongValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);
    if (cachedClient != null && timeoutMillis == cachedTimeoutMillis) {
      return cachedClient;
    }

    cachedClient = createRestClient(timeoutMillis);
    cachedTimeoutMillis = timeoutMillis;
    return cachedClient;
  }

  private RestClient createRestClient(long timeoutMillis) {
    var httpClientBuilder = HttpClient.newBuilder();
    if (timeoutMillis > 0) {
      httpClientBuilder.connectTimeout(Duration.ofMillis(timeoutMillis));
    }

    var requestFactory = new JdkClientHttpRequestFactory(httpClientBuilder.build());
    if (timeoutMillis > 0) {
      requestFactory.setReadTimeout(Duration.ofMillis(timeoutMillis));
    }

    return RestClient.builder()
        .requestFactory(requestFactory)
        .build();
  }
}
