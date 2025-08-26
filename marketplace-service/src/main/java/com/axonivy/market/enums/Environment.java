package com.axonivy.market.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Environment {
  LOCALHOST("localhost"), STAGING("staging"), PRODUCTION("production");

  private final String code;
}
