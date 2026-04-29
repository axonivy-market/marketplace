package com.axonivy.market.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Environment enumeration defining deployment environments for the marketplace service.
 * </p>
 *
 * @since 15/04/2026
 * @author nqhoan
 */
@Getter
@RequiredArgsConstructor
public enum Environment {
  LOCALHOST("localhost"), STAGING("staging"), PRODUCTION("production");

  private final String code;
}
