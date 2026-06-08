package com.axonivy.market.security;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityJwtProperties {

  private static final int MIN_SECRET_LENGTH_BYTES = 32;

  @NotBlank
  private String secret;

  @Min(1)
  private long expirationMinutes = 120;

  @NotBlank
  private String issuer = "marketplace-service";

  @NotBlank
  private String audience = "marketplace-admin-api";

  @PostConstruct
  public void validateSecretStrength() {
    var secretLength = secret.getBytes(StandardCharsets.UTF_8).length;
    if (secretLength < MIN_SECRET_LENGTH_BYTES) {
      throw new IllegalStateException("security.jwt.secret must be at least 32 bytes long");
    }
  }
}
