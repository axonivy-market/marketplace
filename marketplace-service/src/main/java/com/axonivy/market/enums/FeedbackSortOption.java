package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
@AllArgsConstructor
public enum FeedbackSortOption {
  NEWEST("newest", "updatedAt rating _id",
      List.of(Sort.Direction.DESC, Sort.Direction.DESC, Sort.Direction.ASC)),
  OLDEST("oldest", "updatedAt rating _id",
      List.of(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC)),
  HIGHEST("highest", "rating updatedAt _id",
      List.of(Sort.Direction.DESC, Sort.Direction.DESC, Sort.Direction.ASC)),
  LOWEST("lowest", "rating updatedAt _id",
      List.of(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC));

  private final String option;
  private final String code;
  private final List<Sort.Direction> directions;

  public static FeedbackSortOption of(String option) {
    option = StringUtils.isBlank(option) ? option : option.trim();
    for (var feedbackSortOption : values()) {
      if (StringUtils.equalsIgnoreCase(feedbackSortOption.option, option)) {
        return feedbackSortOption;
      }
    }
    throw new InvalidParamException(ErrorCode.FEEDBACK_SORT_INVALID, "SortOption: " + option);
  }
}
