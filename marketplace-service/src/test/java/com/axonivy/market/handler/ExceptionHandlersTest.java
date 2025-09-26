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
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlersTest {

  @InjectMocks
  private ExceptionHandlers exceptionHandlers;

  @BeforeEach
  public void setUp() {
    exceptionHandlers = new ExceptionHandlers();
  }

  @Test
  void testHandleMethodArgumentNotValidWithFieldErrorsReflection() throws Exception {
    FieldError fieldError = new FieldError("objectName", "field1", "Field1 is invalid");
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.hasErrors()).thenReturn(true);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
        new MethodParameter(this.getClass().getDeclaredMethods()[0], -1),
        bindingResult
    );

    ExceptionHandlers handlers = new ExceptionHandlers();

    // Use reflection to access the protected method
    Method method = ExceptionHandlers.class.getDeclaredMethod(
        "handleMethodArgumentNotValid",
        MethodArgumentNotValidException.class, HttpHeaders.class, HttpStatusCode.class, WebRequest.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    ResponseEntity<Object> response = (ResponseEntity<Object>) method.invoke(
        handlers, ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

    assertNotNull(response, "ResponseEntity should not be null");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Response status should be BAD_REQUEST");
    assertInstanceOf(Message.class, response.getBody(), "Response body should be a Message object");
    Message msg = (Message) response.getBody();
    assertTrue(msg.getMessageDetails().contains("Field1 is invalid"),
        "Message details should include the field error");
  }

  @Test
  void testHandleMethodArgumentNotValidElseBranch() throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    BindingResult bindingResult = mock(BindingResult.class);

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
        new MethodParameter(this.getClass().getDeclaredMethods()[0], -1),
        bindingResult
    );

    when(bindingResult.hasErrors()).thenReturn(false);

    ExceptionHandlers handlers = new ExceptionHandlers();

    // Use reflection to access the protected method
    Method method = ExceptionHandlers.class.getDeclaredMethod(
        "handleMethodArgumentNotValid",
        MethodArgumentNotValidException.class, HttpHeaders.class, HttpStatusCode.class, WebRequest.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    ResponseEntity<Object> response = (ResponseEntity<Object>) method.invoke(
        handlers, ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

    assertNotNull(response, "ResponseEntity should not be null");
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "Response status should be BAD_REQUEST");
    assertInstanceOf(Message.class, response.getBody(), "Response body should be a Message object");
    Message msg = (Message) response.getBody();
    assertTrue(msg.getMessageDetails().contains(ex.getMessage()),
        "Message details should include the field error");
  }

  @Test
  void testHandleMissingServletRequestParameter() {
    var errorMessageText = "Missing header";
    var missingHeaderException = mock(MissingHeaderException.class);
    when(missingHeaderException.getMessage()).thenReturn(errorMessageText);

    var responseEntity = exceptionHandlers.handleMissingServletRequestParameter(missingHeaderException);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
    Message errorMessage = (Message) responseEntity.getBody();
    assertEquals(errorMessageText, errorMessage.getMessageDetails(),
        "Expected error message details to be " + errorMessageText);
  }

  @Test
  void testHandleNotFoundException() {
    var notFoundException = mock(NotFoundException.class);
    var responseEntity = exceptionHandlers.handleNotFoundException(notFoundException);
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
        "Expected HTTP 404 NOT_FOUND");
  }

  @Test
  void testHandleNoContentException() {
    var noContentException = mock(NoContentException.class);
    var responseEntity = exceptionHandlers.handleNoContentException(noContentException);
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode(),
        "Expected HTTP 204 NO_CONTENT");
  }

  @Test
  void testHandleInvalidException() {
    var invalidParamException = mock(InvalidParamException.class);
    var responseEntity = exceptionHandlers.handleInvalidException(invalidParamException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
  }

  @Test
  void testHandleOauth2ExchangeCodeException() {
    var oauth2ExchangeCodeException = mock(Oauth2ExchangeCodeException.class);
    var responseEntity = exceptionHandlers.handleOauth2ExchangeCodeException(oauth2ExchangeCodeException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
  }

  @Test
  void testHandleUnauthorizedException() {
    var unauthorizedException = mock(UnauthorizedException.class);
    var responseEntity = exceptionHandlers.handleUnauthorizedException(unauthorizedException);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode(),
        "Expected HTTP 401 UNAUTHORIZED");
  }

  @Test
  void testHandleConstraintViolation() {
    var invalidUrlException = mock(ConstraintViolationException.class);
    var responseEntity = exceptionHandlers.handleConstraintViolation(invalidUrlException);
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
        "Url not found or invalid");
  }
}
