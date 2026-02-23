package com.axonivy.market.stable.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketApiDocumentConfig {

  @Value("${market.info.title}")
  private String title;
  @Value("${market.info.description}")
  private String description;
  @Value("${market.info.version}")
  private String version;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().info(new Info().title(title).description(description).version(version));
  }

}
