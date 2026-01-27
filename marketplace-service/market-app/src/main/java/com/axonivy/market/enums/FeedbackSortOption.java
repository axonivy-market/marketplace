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
  NEWEST("newest", "updatedAt rating id",
      List.of(Sort.Direction.DESC, Sort.Direction.DESC, Sort.Direction.ASC)),
  OLDEST("oldest", "updatedAt rating id",
      List.of(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC)),
  HIGHEST("highest", "rating updatedAt id",
      List.of(Sort.Direction.DESC, Sort.Direction.DESC, Sort.Direction.ASC)),
  LOWEST("lowest", "rating updatedAt id",
      List.of(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC));

  private final String option;
  private final String code;
  private final List<Sort.Direction> directions;

  public static FeedbackSortOption of(String option) {
    if (StringUtils.isNotBlank(option)) {
      option = option.trim();
    }
    for (var feedbackSortOption : values()) {
      if (StringUtils.equalsIgnoreCase(feedbackSortOption.option, option)) {
        return feedbackSortOption;
      }
    }
    throw new InvalidParamException(ErrorCode.FEEDBACK_SORT_INVALID, "FeedbackSortOption: " + option);
  }
}
