package com.axonivy.market.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "market.passkey")
public class PasskeyProperties {
  private String rpId;
  private String rpName;
  private String origins;
  private long timeoutMs = 300000;
}
