package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum SortDirection {
  ASC("asc"),
  DESC("desc");

  private String direction;

  public static SortDirection of(String direction) {
    direction = StringUtils.isBlank(direction) ? direction : direction.trim();
    for (var sortDirection : values()) {
      if (StringUtils.equalsIgnoreCase(sortDirection.direction, direction)) {
        return sortDirection;
      }
    }
    throw new InvalidParamException(ErrorCode.SORT_DIRECTION_INVALID, "SortDirection: " + direction);
  }
}