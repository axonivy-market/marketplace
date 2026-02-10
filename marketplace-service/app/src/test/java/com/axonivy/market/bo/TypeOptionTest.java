package com.axonivy.market.bo;

import com.axonivy.market.core.enums.TypeOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeOptionTest {
  @Test
  void testShouldReturnConnectorsForValidInput() {
    String input = "connectors";
    TypeOption result = TypeOption.of(input);

    assertEquals(TypeOption.CONNECTORS, result,
        "Expected TypeOption.of('" + input + "') to return CONNECTORS");
  }

  @Test
  void testShouldReturnALLIgnoringCase() {
    String input = "ALL";
    TypeOption result = TypeOption.of(input);

    assertEquals(TypeOption.ALL, result,
        "Expected TypeOption.of('" + input + "') to return ALL");
  }

  @Test
  void testShouldReturnAllForUnknownOption() {
    String input = "foo";
    TypeOption result = TypeOption.of(input);
    assertEquals(TypeOption.ALL, result,
        "Expected TypeOption.of('" + input + "') to return ALL");
  }
}
