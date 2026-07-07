package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

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
      var timeout = Duration.ofMillis(timeoutMillis);
      httpClientBuilder.connectTimeout(timeout);
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
