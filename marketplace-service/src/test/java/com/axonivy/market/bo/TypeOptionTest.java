package com.axonivy.market.bo;

import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.exceptions.model.InvalidParamException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeOptionTest {
  @Test
  void shouldReturnConnectorsForValidInput() {
    String input = "connectors";
    TypeOption result = TypeOption.of(input);

    assertEquals(TypeOption.CONNECTORS, result,
        "Expected TypeOption.of('" + input + "') to return CONNECTORS");
  }

  @Test
  void shouldReturnALLIgnoringCase() {
    String input = "ALL";
    TypeOption result = TypeOption.of(input);

    assertEquals(TypeOption.ALL, result,
        "Expected TypeOption.of('" + input + "') to return ALL");
  }

  @Test
  void shouldThrowExceptionForNull() {
    Exception ex = assertThrows(InvalidParamException.class,
        () -> TypeOption.of(null),
        "Expected TypeOption.of(null) to throw InvalidParamException");
    assertTrue(ex.getMessage().contains("TypeOption: null"),
        "Exception message should contain 'TypeOption: null'");
  }

  @Test
  void shouldThrowExceptionForUnknownOption() {
    String input = "foo";
    Exception ex = assertThrows(InvalidParamException.class,
        () -> TypeOption.of(input),
        "Expected TypeOption.of('" + input + "') to throw InvalidParamException");
    assertTrue(ex.getMessage().contains("TypeOption: foo"),
        "Exception message should contain 'TypeOption: foo'");
  }
}
