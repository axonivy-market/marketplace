package com.axonivy.market.exceptions;

import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.model.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MarketExceptionHandler {

  @ExceptionHandler(MissingHeaderException.class)
  public ResponseEntity<Object> handleMissingServletRequestParameter(Throwable missingHeaderException) {
    var errorMessage = new Message();
    errorMessage.setMessageDetails(missingHeaderException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Object> handleNotFoundException(NotFoundException notFoundException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(notFoundException.getCode());
    errorMessage.setMessageDetails(notFoundException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoContentException.class)
  public ResponseEntity<Object> handleNoContentException(NoContentException noContentException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(noContentException.getCode());
    errorMessage.setMessageDetails(noContentException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.NO_CONTENT);
  }

  @ExceptionHandler(InvalidParamException.class)
  public ResponseEntity<Object> handleInvalidException(InvalidParamException invalidDataException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(invalidDataException.getCode());
    errorMessage.setMessageDetails(invalidDataException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Oauth2ExchangeCodeException.class)
  public ResponseEntity<Object> handleOauth2ExchangeCodeException(
      Oauth2ExchangeCodeException oauth2ExchangeCodeException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(oauth2ExchangeCodeException.getError());
    errorMessage.setMessageDetails(oauth2ExchangeCodeException.getErrorDescription());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorizedException(
      UnauthorizedException unauthorizedException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(unauthorizedException.getError());
    errorMessage.setMessageDetails(unauthorizedException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidZipEntryException.class)
  public ResponseEntity<Map<String, String>> handleInvalidZipEntry(InvalidZipEntryException ex) {
    Map<String, String> body = new HashMap<>();
    body.put("message", "Invalid zip entry detected: " + ex.getMessage() + ", skipped this file");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<Object> handleIOException(IOException ex) {
    Map<String, String> body = new HashMap<>();
    body.put("message", "Sorry, there was a problem processing your request. Please try again or contact support");
    body.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}