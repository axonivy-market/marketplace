package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.data.domain.Sort;

import java.util.List;

public class FeedbackSortOptionTest {
  @Test
  void testOfValidOptions() {
    assertEquals(FeedbackSortOption.NEWEST, FeedbackSortOption.of("newest"),
        "Expected 'newest' to resolve to NEWEST");
    assertEquals(FeedbackSortOption.OLDEST, FeedbackSortOption.of("oldest"),
        "Expected 'oldest' to resolve to OLDEST");
    assertEquals(FeedbackSortOption.HIGHEST, FeedbackSortOption.of("highest"),
        "Expected 'highest' to resolve to HIGHEST");
    assertEquals(FeedbackSortOption.LOWEST, FeedbackSortOption.of("lowest"),
        "Expected 'lowest' to resolve to LOWEST");
  }

  @Test
  void testOfCaseInsensitive() {
    assertEquals(FeedbackSortOption.NEWEST, FeedbackSortOption.of("NeWeSt"),
        "Expected case-insensitive match for NEWEST");
    assertEquals(FeedbackSortOption.OLDEST, FeedbackSortOption.of("  OLDEST  "),
        "Expected trimmed case-insensitive match for OLDEST");
  }

  @Test
  void testOfTrimmedInput() {
    assertEquals(FeedbackSortOption.HIGHEST, FeedbackSortOption.of("  highest "),
        "Expected trimmed input to resolve to HIGHEST");
  }

  @Test
  void testOfInvalidOptionThrowsException() {
    InvalidParamException ex = assertThrows(
        InvalidParamException.class,
        () -> FeedbackSortOption.of("invalid")
    );
    assertTrue(ex.getMessage().contains("FeedbackSortOption: invalid"),
        "Expected exception message to contain the invalid option");
  }

  @Test
  void testOfBlankOrNullThrowsException() {
    assertThrows(InvalidParamException.class,
        () -> FeedbackSortOption.of(""),
        "Expected InvalidParamException for empty string");
    assertThrows(InvalidParamException.class,
        () -> FeedbackSortOption.of("   "),
        "Expected InvalidParamException for blank string");
    assertThrows(InvalidParamException.class,
        () -> FeedbackSortOption.of(null),
        "Expected InvalidParamException for null input");
  }

  @Test
  void testEnumFieldValues() {
    assertEquals("newest", FeedbackSortOption.NEWEST.getOption(),
        "Expected 'option' field of NEWEST to be 'newest'");
    assertEquals("updatedAt rating id", FeedbackSortOption.NEWEST.getCode(),
        "Expected 'code' field of NEWEST to match");
    assertEquals(List.of(Sort.Direction.DESC, Sort.Direction.DESC, Sort.Direction.ASC),
        FeedbackSortOption.NEWEST.getDirections(),
        "Expected 'directions' field of NEWEST to match expected values");
  }
}
