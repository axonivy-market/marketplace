package com.axonivy.market.stable.strategy.impl;

import com.axonivy.market.core.exceptions.model.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SameMajorVersionStrategyTest {
  private final SameMajorVersionStrategy strategy = new SameMajorVersionStrategy();

  @Test
  void testShouldReturnFirstMatchingSameMajorVersion() {
    List<String> versions = List.of("2.1", "2.0", "1.5");
    String result = strategy.findMatch(versions, "2.3");

    assertEquals("2.1", result, "Expected first version with same major (2.x) to be returned");
  }

  @Test
  void testShouldRespectOrderAndReturnFirstMatch() {
    List<String> versions = List.of("2.9", "2.1", "2.0");
    String result = strategy.findMatch(versions, "2.5");

    assertEquals("2.9", result, "Expected first matching version in list order, not best or closest");
  }

  @Test
  void testShouldThrowWhenVersionsListIsEmpty() {
    List<String> versions = List.of();

    assertThrows(
        NotFoundException.class,
        () -> strategy.findMatch(versions, "1.0"),
        "Expected NotFoundException when versions list is empty"
    );
  }

  @Test
  void testShouldThrowWhenVersionsListIsNull() {
    assertThrows(
        NotFoundException.class,
        () -> strategy.findMatch(null, "1.0"),
        "Expected NotFoundException when versions list is null"
    );
  }

  @Test
  void testShouldThrowWhenNoMatchingMajorVersionFound() {
    List<String> versions = List.of("1.0", "1.5", "3.0");
    assertThrows(
        NotFoundException.class,
        () -> strategy.findMatch(versions, "2.0"),
        "Expected NotFoundException when no matching major version exists"
    );
  }

  @Test
  void testShouldIgnoreVersionsWithoutMatchingMajorEvenIfFormatDiffers() {
    List<String> versions = List.of("2", "2.1", "3.0");
    String result = strategy.findMatch(versions, "2.5");

    assertEquals("2", result,
        "Expected version '2' to match major version 2");
  }
}
