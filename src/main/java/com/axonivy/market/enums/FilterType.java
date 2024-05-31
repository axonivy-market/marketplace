package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public enum FilterType {
  ALL("all"), CONNECTORS("connectors"), UTILITIES("utilities"), SOLUTIONS("solutions");

  private String type;

  private FilterType(String type) {
    this.type = type;
  }

  public static FilterType of(String type) {
    type = StringUtils.isBlank(type) ? type : type.trim();
    for (var filter : values()) {
      if (StringUtils.equalsIgnoreCase(filter.type, type)) {
        return filter;
      }
    }
    return ALL;
  }
}
