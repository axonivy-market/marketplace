package com.axonivy.market.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TestStatus {
  PASSED,
  FAILED,
  SKIPPED;

  @JsonValue
  public String toValue() {
    return name().toLowerCase();
  }
}
