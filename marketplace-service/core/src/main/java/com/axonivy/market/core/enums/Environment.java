package com.axonivy.market.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Environment {
  LOCALHOST("localhost"), STAGING("staging"), PRODUCTION("production");

  private final String code;
}
