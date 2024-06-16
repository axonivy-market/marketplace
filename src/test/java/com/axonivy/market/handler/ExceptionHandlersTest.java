package com.axonivy.market.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.axonivy.market.exceptions.ExceptionHandlers;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.Message;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlersTest {

  private ExceptionHandlers exceptionHandlers;

  @Mock
  private MissingHeaderException missingHeaderException;

  @Mock
  private NotFoundException notFoundException;

  @BeforeEach
  public void setUp() {
    exceptionHandlers = new ExceptionHandlers();
  }

  @Test
  void testHandleMissingServletRequestParameter() {
    String errorMessageText = "Missing header";
    when(missingHeaderException.getMessage()).thenReturn(errorMessageText);

    var responseEntity = exceptionHandlers.handleMissingServletRequestParameter(missingHeaderException);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    Message errorMessage = (Message) responseEntity.getBody();
    assertEquals(errorMessageText, errorMessage.getMessageDetails());
  }

  @Test
  void testHandleNotFoundException() {
    String errorMessageText = "Not found";
    when(notFoundException.getMessage()).thenReturn(errorMessageText);

    var responseEntity = exceptionHandlers.handleNotFoundException(notFoundException);

    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    Message errorMessage = (Message) responseEntity.getBody();
    assertEquals("-1", errorMessage.getErrorCode());
    assertEquals(errorMessageText, errorMessage.getMessageDetails());
  }
}
