package com.axonivy.market.github.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
@Getter
@Setter
@RequiredArgsConstructor
public class SecurityMonitorMailProperties {
  private String from;
  private String to;
}
