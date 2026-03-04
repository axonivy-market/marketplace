package com.axonivy.market.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
      if (StringUtils.equalsIgnoreCase(filter.option, option)) {
        return filter;
      }
    }
    return ALL;
  }
}
