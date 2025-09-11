package com.axonivy.market.exceptions;

import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.exceptions.model.InvalidParamException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InvalidParamExceptionTest {
  @Test
  void testConstructorWithCodeAndMessage() {
    ErrorCode code = ErrorCode.PRODUCT_SORT_INVALID;
    String sortOption = "SortOption: " + SortOption.ALPHABETICALLY;

    InvalidParamException exception = new InvalidParamException(code.getCode(), sortOption);

    assertNotNull(exception, "Exception instance should not be null");
    assertEquals(code.getCode(), exception.getCode(),
        "Code should match the one provided to the constructor");
    assertEquals(sortOption, exception.getMessage(),
        "Message should match the one provided to the constructor");
  }

  @Test
  void testConstructorWithErrorCode() {
    ErrorCode code = ErrorCode.PRODUCT_SORT_INVALID;

    InvalidParamException exception = new InvalidParamException(code);

    assertNotNull(exception, "Exception instance should not be null");
    assertEquals(code.getCode(), exception.getCode(),
        "Code should match the one from ErrorCode");
    assertEquals(code.getHelpText(), exception.getMessage(),
        "Message should match the one from ErrorCode");
  }

  @Test
  void testConstructorWithErrorCodeAndAdditionalMessage() {
    ErrorCode code = ErrorCode.PRODUCT_SORT_INVALID;
    String sortOption = "SortOption: " + SortOption.ALPHABETICALLY;

    InvalidParamException exception = new InvalidParamException(code, sortOption);

    assertNotNull(exception, "Exception instance should not be null");
    assertEquals(code.getCode(), exception.getCode(),
        "Code should match the one from ErrorCode");
    assertTrue(exception.getMessage().contains(sortOption),
        "Message should include the additional message provided");
  }
}
