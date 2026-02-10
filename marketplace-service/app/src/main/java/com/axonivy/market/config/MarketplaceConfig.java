package com.axonivy.market.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.axonivy.market.enums.Environment;

import lombok.Getter;

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
