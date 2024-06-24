package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.market.exceptions.model.InvalidParamException;

import lombok.Getter;

@Getter
public enum TypeOption {
  ALL("all", ""), CONNECTORS("connectors", "connector"), UTILITIES("utilities", "util"), SOLUTIONS("solutions", "solution"), DEMOS("demos", "demo");

  private String option;
  private String code;

  private TypeOption(String option, String code) {
    this.option = option;
    this.code = code;
  }

  public static TypeOption of(String option) {
    option = StringUtils.isBlank(option) ? option : option.trim();
    for (var filter : values()) {
      if (StringUtils.equalsIgnoreCase(filter.option, option)) {
        return filter;
      }
    }
    throw new InvalidParamException(ErrorCode.PRODUCT_FILTER_INVALID, "TypeOption: " + option);
  }
}
