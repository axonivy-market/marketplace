package com.axonivy.market.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TestEnviroment {
  OTHER,
  ALL,
  MOCK,
  REAL;

  @JsonValue
  public String toValue() {
    return name().toLowerCase();
  }
}
