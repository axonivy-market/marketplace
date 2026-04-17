package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class SecretScanning {
  private Integer numberOfSecretScanningAlerts;
  @Enumerated(EnumType.STRING)
  private AccessLevel status;
}
