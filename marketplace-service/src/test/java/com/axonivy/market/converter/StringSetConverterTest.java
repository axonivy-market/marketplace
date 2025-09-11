package com.axonivy.market.converter;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class StringSetConverterTest {
  private final StringSetConverter converter = new StringSetConverter();

  @Test
  void testCreateCollectionWithElements() {
    Collection<String> input = List.of("one", "two", "three");

    Set<String> result = converter.createCollection(input);

    assertEquals(3, result.size(), "Result should have the same number of unique elements as input");
    assertTrue(result.containsAll(input), "Result should contain all elements from input");
    assertInstanceOf(HashSet.class, result, "Result should be a HashSet");

    // Ensure it's a new mutable set, not just wrapping the input
    result.add("four");
    assertTrue(result.contains("four"), "Result should allow modifications");
    assertFalse(input.contains("four"), "Input should not be modified");
  }

  @Test
  void testCreateCollectionWithDuplicates() {
    Collection<String> input = List.of("one", "two", "two", "three");

    Set<String> result = converter.createCollection(input);

    assertEquals(3, result.size(), "Result should remove duplicates");
    assertTrue(result.contains("two"), "Result should still contain 'two'");
  }

  @Test
  void testCreateCollectionEmptyInput() {
    Collection<String> input = Collections.emptyList();

    Set<String> result = converter.createCollection(input);

    assertTrue(result.isEmpty(), "Result should be empty when input is empty");
    assertInstanceOf(HashSet.class, result, "Result should be a HashSet");
  }
}
