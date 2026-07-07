package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Access level enumeration defining permission states for user access control and authorization.
 * </p>
 *
 * @since 15/04/2026
 * @author ndkhanh
 */
@Getter
@AllArgsConstructor
public enum AccessLevel {
  ENABLED, NO_PERMISSION, NOT_SUPPORTED, DISABLED;
}
