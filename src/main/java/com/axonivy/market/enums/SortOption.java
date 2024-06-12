package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.market.exceptions.NotFoundException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortOption {
  Popularity("popularity", "installationCount"), Alphabetically("alphabetically", "name"), Recent("recent", "newestPublishDate");

  private String option;
  private String code;

  public static SortOption of(String option) {
    option = StringUtils.isBlank(option) ? option : option.trim();
    for (var sortOption : values()) {
      if (StringUtils.equalsIgnoreCase(sortOption.option, option)) {
        return sortOption;
      }
    }
    throw new NotFoundException("Not found sort option: " + option);
  }
}
