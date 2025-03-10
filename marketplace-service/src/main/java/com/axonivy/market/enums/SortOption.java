package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
public enum SortOption {
  POPULARITY("popularity", "marketplaceData.installationCount", Sort.Direction.DESC),
  ALPHABETICALLY("alphabetically", "names", Sort.Direction.ASC),
  RECENT("recent", "firstPublishedDate", Sort.Direction.DESC),
  STANDARD("standard", "marketplaceData.customOrder", Sort.Direction.DESC),
  ID("id", "_id", Sort.Direction.ASC);

  private final String option;
  private final String code;
  private final Sort.Direction direction;

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
    return StringUtils.isNotBlank(language) && ALPHABETICALLY.option.equalsIgnoreCase(option) ? String.format("%s.%s",
        ALPHABETICALLY.code, language) : code;
  }
}
