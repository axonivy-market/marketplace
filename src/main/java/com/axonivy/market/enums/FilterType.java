package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.market.exceptions.model.NotFoundException;

import lombok.Getter;

@Getter
public enum FilterType {
  ALL("all", ""), CONNECTORS("connectors", "connector"), UTILITIES("utilities", "util"), SOLUTIONS("solutions", "solution"), DEMOS("demos", "demo");

  private String option;
  private String code;

  private FilterType(String option, String code) {
    this.option = option;
    this.code = code;
  }

  public static FilterType of(String option) {
    option = StringUtils.isBlank(option) ? option : option.trim();
    for (var filter : values()) {
      if (StringUtils.equalsIgnoreCase(filter.option, option)) {
        return filter;
      }
    }
    throw new NotFoundException("Not found filter option: " + option);
  }
}
