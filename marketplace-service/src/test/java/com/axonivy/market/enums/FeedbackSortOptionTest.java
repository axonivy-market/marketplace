package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.data.domain.Sort;

import java.util.List;

public class FeedbackSortOptionTest {
  @Test
  void testOfValidOptions() {
    assertEquals(FeedbackSortOption.NEWEST, FeedbackSortOption.of("newest"));
    assertEquals(FeedbackSortOption.OLDEST, FeedbackSortOption.of("oldest"));
    assertEquals(FeedbackSortOption.HIGHEST, FeedbackSortOption.of("highest"));
    assertEquals(FeedbackSortOption.LOWEST, FeedbackSortOption.of("lowest"));
  }

  @Test
  void testOfCaseInsensitive() {
    assertEquals(FeedbackSortOption.NEWEST, FeedbackSortOption.of("NeWeSt"));
    assertEquals(FeedbackSortOption.OLDEST, FeedbackSortOption.of("  OLDEST  "));
  }

  @Test
  void testOfTrimmedInput() {
    assertEquals(FeedbackSortOption.HIGHEST, FeedbackSortOption.of("  highest "));
  }

  @Test
  void testOfInvalidOptionThrowsException() {
    InvalidParamException ex = assertThrows(
        InvalidParamException.class,
        () -> FeedbackSortOption.of("invalid")
    );
    assertTrue(ex.getMessage().contains("FeedbackSortOption: invalid"));
    assertEquals(ErrorCode.FEEDBACK_SORT_INVALID, ErrorCode.FEEDBACK_SORT_INVALID);
  }

  @Test
  void testOf_blankOrNull_throwsException() {
    assertThrows(InvalidParamException.class, () -> FeedbackSortOption.of(""));
    assertThrows(InvalidParamException.class, () -> FeedbackSortOption.of("   "));
    assertThrows(InvalidParamException.class, () -> FeedbackSortOption.of(null));
  }

  @Test
  void testEnumFieldValues() {
    assertEquals("newest", FeedbackSortOption.NEWEST.getOption());
    assertEquals("updatedAt rating id", FeedbackSortOption.NEWEST.getCode());
    assertEquals(List.of(Sort.Direction.DESC, Sort.Direction.DESC, Sort.Direction.ASC),
        FeedbackSortOption.NEWEST.getDirections());
  }
}
