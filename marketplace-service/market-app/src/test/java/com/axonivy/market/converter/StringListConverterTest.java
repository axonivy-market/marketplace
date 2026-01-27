package com.axonivy.market.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

class StringListConverterTest {
  private final StringListConverter converter = new StringListConverter();

  @Test
  void testCreateCollectionWithElements() {
    Collection<String> input = Set.of("one", "two", "three");

    List<String> result = converter.createCollection(input);

    assertThat(result)
        .as("Result should contain the same elements as input")
        .containsExactlyInAnyOrder("one", "two", "three")
        .isInstanceOf(ArrayList.class);

    result.add("four");
    assertThat(result)
        .as("Result should contain four and is a different collection from the original input")
        .contains("four");
    assertThat(input).as("Original input should not contain four").doesNotContain("four");
  }
}
