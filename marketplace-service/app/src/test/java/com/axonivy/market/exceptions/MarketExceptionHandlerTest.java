package com.axonivy.market.exceptions;

import com.axonivy.market.core.exceptions.model.InvalidParamException;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.*;
import com.axonivy.market.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class MarketExceptionHandlerTest {

  @InjectMocks
  private MarketExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new MarketExceptionHandler();
  }

  @Test
  void testHandleMissingServletRequestParameter() {
    var errorMessageText = "Missing header";
    var missingHeaderException = mock(MissingHeaderException.class);
    when(missingHeaderException.getMessage()).thenReturn(errorMessageText);

    var responseEntity = exceptionHandler.handleMissingServletRequestParameter(missingHeaderException);

    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
    Message errorMessage = (Message) responseEntity.getBody();
    assertNotNull(errorMessage, "Expected error message body to be not null");
    assertEquals(errorMessageText, errorMessage.getMessageDetails(),
        "Expected error message details to be " + errorMessageText);
  }

  @Test
  void testHandleNotFoundException() {
    var notFoundException = mock(NotFoundException.class);
    var responseEntity = exceptionHandler.handleNotFoundException(notFoundException);
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
        "Expected HTTP 404 NOT_FOUND");
  }

  @Test
  void testHandleNoContentException() {
    var noContentException = mock(NoContentException.class);
    var responseEntity = exceptionHandler.handleNoContentException(noContentException);
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode(),
        "Expected HTTP 204 NO_CONTENT");
  }

  @Test
  void testHandleInvalidException() {
    var invalidParamException = mock(InvalidParamException.class);
    var responseEntity = exceptionHandler.handleInvalidException(invalidParamException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
  }

  @Test
  void testHandleOauth2ExchangeCodeException() {
    var oauth2ExchangeCodeException = mock(Oauth2ExchangeCodeException.class);
    var responseEntity = exceptionHandler.handleOauth2ExchangeCodeException(oauth2ExchangeCodeException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
  }

  @Test
  void testHandleUnauthorizedException() {
    var unauthorizedException = mock(UnauthorizedException.class);
    var responseEntity = exceptionHandler.handleUnauthorizedException(unauthorizedException);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode(),
        "Expected HTTP 401 UNAUTHORIZED");
  }

  @Test
  void testHandleInvalidZipEntry() {
    InvalidZipEntryException exception = new InvalidZipEntryException("Missing required file: README.md");
    ResponseEntity<Map<String, String>> response = exceptionHandler.handleInvalidZipEntry(exception);
    assertEquals(403, response.getStatusCode().value(), "Should return HTTP 403 Forbidden");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertTrue(response.getBody().get("message").contains("Missing required file: README.md"),
        "Message should include exception detail");
    assertTrue(response.getBody().get("message").contains("Invalid zip entry detected:"),
        "Message should start with expected prefix");
    assertTrue(response.getBody().get("message").endsWith(", skipped this file"),
        "Message should end with expected suffix");
  }

  @Test
  void testHandleFileProcess() {
    var fileProcessingException = mock(FileProcessingException.class);
    var responseEntity = exceptionHandler.handleFileProcess(fileProcessingException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
  }

  @Test
  void testHandleIOException() {
    IOException ioException = new IOException("File read error");
    var responseEntity = exceptionHandler.handleIOException(ioException);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode(),
        "Expected HTTP 500 INTERNAL_SERVER_ERROR");
  }

  @Test
  void testHandleIllegalArgumentException() {
    IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Invalid argument");
    var responseEntity = exceptionHandler.handleIllegalArgumentException(illegalArgumentException);
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
        "Expected HTTP 400 BAD_REQUEST");
  }

  @Test
  void testHandleTaskAlreadyRunningException() {
    TaskAlreadyRunningException taskAlreadyRunningException = new TaskAlreadyRunningException(
        "Task is already running!");
    var responseEntity = exceptionHandler.handleTaskAlreadyRunningException(taskAlreadyRunningException);
    assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(),
        "Expected HTTP 202 ACCEPTED");
    assertNotNull(responseEntity.getBody(), "Response body should not be null");
  }

  @Test
  void shouldHandleValidationExceptions() {
    // Arrange
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);

    FieldError fieldError1 = new FieldError("object", "sprint", "Sprint cannot be blank");
    FieldError fieldError2 = new FieldError("object", "version", "Version is required");

    when(exception.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "HTTP status should be 400 BAD_REQUEST when validation fails");

    Map<String, Object> body = response.getBody();
    assertNotNull(body, "Response body must not be null for validation errors");

    assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"),
        "Response body 'status' field should contain 400");

    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) body.get("errors");

    assertEquals(2, errors.size(), "Validation error map should contain exactly 2 field errors");
    assertEquals("Sprint cannot be blank", errors.get("sprint"),
        "Error message for field 'sprint' is incorrect");
    assertEquals("Version is required", errors.get("version"),
        "Error message for field 'version' is incorrect");
  }

  @Test
  void shouldHandleMarketException() {
    MarketException exception = mock(MarketException.class);

    when(exception.getCode()).thenReturn("ERR_001");
    when(exception.getMessage()).thenReturn("Sprint cannot be blank");

    ResponseEntity<Object> response = exceptionHandler.handleAMarketException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "HTTP status should be 400 BAD_REQUEST for MarketException");

    Message body = (Message) response.getBody();
    assertNotNull(body, "Response body must not be null for MarketException");

    assertEquals("ERR_001", body.getHelpCode(), "Help code in response does not match exception code");
    assertEquals("Sprint cannot be blank", body.getMessageDetails(),
        "Message details in response do not match exception message");
  }

  @Test
  void shouldHandleAlreadyExistedException() {
    AlreadyExistedException exception = mock(AlreadyExistedException.class);

    when(exception.getCode()).thenReturn("ERR_409");
    when(exception.getMessage()).thenReturn("Release letter already exists");

    ResponseEntity<Object> response = exceptionHandler.handleAlreadyExistedException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "HTTP status should be 400 BAD_REQUEST for AlreadyExistedException");

    Message body = (Message) response.getBody();
    assertNotNull(body, "Response body must not be null for AlreadyExistedException");

    assertEquals("ERR_409", body.getHelpCode(),
        "Help code in response does not match AlreadyExistedException code");
    assertEquals("Release letter already exists", body.getMessageDetails(),
        "Message details in response do not match AlreadyExistedException message");
  }
}
