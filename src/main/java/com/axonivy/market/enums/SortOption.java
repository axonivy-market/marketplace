package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.market.exceptions.model.InvalidParamException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortOption {
  POPULARITY("popularity", "installationCount"), ALPHABETICALLY("alphabetically", "names"),
  RECENT("recent", "newestPublishedDate");

  private String option;
  private String code;

  public static SortOption of(String option) {
    option = StringUtils.isBlank(option) ? option : option.trim();
    for (var sortOption : values()) {
      if (StringUtils.equalsIgnoreCase(sortOption.option, option)) {
        return sortOption;
      }
    }
    throw new InvalidParamException(ErrorCode.PRODUCT_SORT_INVALID, "SortOption: " + option);
  }

  public String getCode(String language) {
    return StringUtils.isNotBlank(language) && ALPHABETICALLY.option.equalsIgnoreCase(option)
        ? String.format("%s.%s", ALPHABETICALLY.code, language)
        : code;
  }
}
