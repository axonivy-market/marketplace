package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SecretScanning {
  private Integer numberOfAlerts;
  private AccessLevel status;
}
