package com.axonivy.market.handler;

import com.axonivy.market.exceptions.ExceptionHandlers;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.model.Message;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlersTest {

  @InjectMocks
  private ExceptionHandlers exceptionHandlers;

  @BeforeEach
  public void setUp() {
    exceptionHandlers = new ExceptionHandlers();
  }

  @Test
  void testHandleMissingServletRequestParameter() {
    var errorMessageText = "Missing header";
    var missingHeaderException = mock(MissingHeaderException.class);
    when(missingHeaderException.getMessage()).thenReturn(errorMessageText);

    var responseEntity = exceptionHandlers.handleMissingServletRequestParameter(missingHeaderException);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    Message errorMessage = (Message) responseEntity.getBody();
    assertEquals(errorMessageText, errorMessage.getMessageDetails());
  }

  @Test
  void testHandleNotFoundException() {
    var notFoundException = mock(NotFoundException.class);
    var responseEntity = exceptionHandlers.handleNotFoundException(notFoundException);
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
  }

  @Test
  void testHandleNoContentException() {
    var noContentException = mock(NoContentException.class);
    var responseEntity = exceptionHandlers.handleNoContentException(noContentException);
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
  }

  @Test
  void testHandleInvalidException() {
    var invalidParamException = mock(InvalidParamException.class);
    var responseEntity = exceptionHandlers.handleInvalidException(invalidParamException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void testHandleOauth2ExchangeCodeException() {
    var oauth2ExchangeCodeException = mock(Oauth2ExchangeCodeException.class);
    var responseEntity = exceptionHandlers.handleOauth2ExchangeCodeException(oauth2ExchangeCodeException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
  }

  @Test
  void testHandleUnauthorizedException() {
    var unauthorizedException = mock(UnauthorizedException.class);
    var responseEntity = exceptionHandlers.handleUnauthorizedException(unauthorizedException);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
  }
  @Test
  void testHandleConstraintViolation() {
    var invalidUrlException = mock(ConstraintViolationException.class);
    var responseEntity = exceptionHandlers.handleConstraintViolation(invalidUrlException);
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Url not found or invalid");
  }
}
