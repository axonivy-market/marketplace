package com.axonivy.market.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * <p>
 * Product type option enumeration defining available product type filtering categories.
 * </p>
 *
 * @since 15/04/2026
 * @author nqhoan
 */
@Getter
@AllArgsConstructor
public enum TypeOption {
  ALL("all", ""), CONNECTORS("connectors", "connector"), UTILITIES("utilities", "util"),
  SOLUTIONS("solutions", "solution"), DEMOS("demos", "demo");

  private final String option;
  private final String code;

  public static TypeOption of(String option) {
    if (StringUtils.isNotBlank(option)) {
      option = option.trim();
    }
    for (var filter : values()) {
      if (Strings.CS.equals(filter.option, option)) {
        return filter;
      }
    }
    return ALL;
  }
}
