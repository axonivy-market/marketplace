package com.axonivy.market.converter;

import jakarta.persistence.AttributeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AbstractCollectionConverterTest {
  // Concrete subclass for testing with List<String>
  static class ListStringConverter extends AbstractCollectionConverter<List<String>> {
    @Override
    protected List<String> createCollection(Collection<String> elements) {
      return new ArrayList<>(elements);
    }
  }

  private AttributeConverter<List<String>, String> converter;

  @BeforeEach
  void setUp() {
    converter = new ListStringConverter();
  }

  @Test
  void testConvertToDatabaseColumnWithValidCollection() {
    List<String> input = Arrays.asList(" A", "B ", " C ");
    String result = converter.convertToDatabaseColumn(input);

    assertEquals("A,B,C", result,
        "convertToDatabaseColumn should trim elements and join with commas");
  }

  @Test
  void testConvertToDatabaseColumnWithEmptyCollection() {
    List<String> input = Collections.emptyList();
    String result = converter.convertToDatabaseColumn(input);

    assertEquals("", result,
        "convertToDatabaseColumn should return empty string for empty collection");
  }

  @Test
  void testConvertToDatabaseColumnWithNullCollection() {
    String result = converter.convertToDatabaseColumn(null);

    assertEquals("", result,
        "convertToDatabaseColumn should return empty string for null collection");
  }

  @Test
  void testConvertToEntityAttributeWithValidString() {
    String dbValue = "A,B,C";
    List<String> result = converter.convertToEntityAttribute(dbValue);

    assertEquals(Arrays.asList("A", "B", "C"), result,
        "convertToEntityAttribute should split string by comma into list");
  }

  @Test
  void testConvertToEntityAttributeWithBlankString() {
    String dbValue = "   ";
    List<String> result = converter.convertToEntityAttribute(dbValue);

    assertTrue(result.isEmpty(),
        "convertToEntityAttribute should return empty collection for blank string");
  }

  @Test
  void testConvertToEntityAttributeWithNullString() {
    List<String> result = converter.convertToEntityAttribute(null);

    assertTrue(result.isEmpty(),
        "convertToEntityAttribute should return empty collection for null input");
  }
}
