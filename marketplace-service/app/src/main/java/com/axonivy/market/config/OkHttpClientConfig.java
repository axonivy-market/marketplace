package com.axonivy.market.config;

import com.axonivy.market.github.model.GitHubProperty;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OkHttpClientConfig {

  @Bean
  public OkHttpClient okHttpClient(GitHubProperty gitHubProperty) {
    return new OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(gitHubProperty.getConnectTimeout()))
        .build();
  }
}

