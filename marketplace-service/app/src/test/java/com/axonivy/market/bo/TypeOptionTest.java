package com.axonivy.market.bo;

import com.axonivy.market.core.enums.TypeOption;
import com.axonivy.market.core.exceptions.model.InvalidParamException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
