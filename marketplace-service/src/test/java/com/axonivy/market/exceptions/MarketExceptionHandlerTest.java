package com.axonivy.market.exceptions;

import com.axonivy.market.exceptions.model.FileProcessingException;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  void testHandleIoException() {
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
}
