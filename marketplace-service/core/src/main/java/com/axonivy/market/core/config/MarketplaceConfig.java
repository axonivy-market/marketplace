package com.axonivy.market.core.config;

import com.axonivy.market.core.enums.Environment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@RequiredArgsConstructor
@Component
public class MarketplaceConfig {
  @Value("${market.environment}")
  private String marketEnvironment;

  public boolean isProduction() {
    return Environment.PRODUCTION.getCode().equals(marketEnvironment);
  }
}
