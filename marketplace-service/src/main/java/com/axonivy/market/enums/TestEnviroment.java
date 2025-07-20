package com.axonivy.market.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.Locale;

@Getter
public enum TestEnviroment {
  OTHER,
  MOCK,
  REAL;

  @JsonValue
  public String toValue() {
    return name().toLowerCase(Locale.ROOT);
  }
}
