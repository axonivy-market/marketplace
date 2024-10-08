package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
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
    option = StringUtils.isBlank(option) ? option : option.trim();
    for (var filter : values()) {
      if (StringUtils.equalsIgnoreCase(filter.option, option)) {
        return filter;
      }
    }
    throw new InvalidParamException(ErrorCode.PRODUCT_FILTER_INVALID, "TypeOption: " + option);
  }
}
