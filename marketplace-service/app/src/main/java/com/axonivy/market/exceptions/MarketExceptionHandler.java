package com.axonivy.market.exceptions;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.InvalidParamException;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.exceptions.model.FileProcessingException;
import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.TaskAlreadyRunningException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.model.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MarketExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException exception) {
    Map<String, Object> errors = new HashMap<>();
    errors.put("status", HttpStatus.BAD_REQUEST.value());

    Map<String, String> fieldErrors = new HashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.put(error.getField(), error.getDefaultMessage())
    );

    errors.put("errors", fieldErrors);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MarketException.class)
  public ResponseEntity<Object> handleAMarketException(MarketException marketException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(marketException.getCode());
    errorMessage.setMessageDetails(marketException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AlreadyExistedException.class)
  public ResponseEntity<Object> handleAlreadyExistedException(AlreadyExistedException alreadyExistedException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(alreadyExistedException.getCode());
    errorMessage.setMessageDetails(alreadyExistedException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

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

  @ExceptionHandler(FileProcessingException.class)
  public ResponseEntity<Object> handleFileProcess(FileProcessingException fileProcessingException) {
    var errorMessage = new Message();
    errorMessage.setHelpCode(fileProcessingException.getCode());
    errorMessage.setMessageDetails(fileProcessingException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<Message> handleIOException(IOException ex) {
    var message = new Message(ErrorCode.INTERNAL_EXCEPTION.getCode(),
        ErrorCode.INTERNAL_EXCEPTION.getHelpText(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Message> handleIllegalArgumentException(IllegalArgumentException ex) {
    var message = new Message(ErrorCode.ARGUMENT_BAD_REQUEST.getCode(), ex.getMessage(),
        ErrorCode.ARGUMENT_BAD_REQUEST.getHelpText());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
  }

  @ExceptionHandler(TaskAlreadyRunningException.class)
  public ResponseEntity<Message> handleTaskAlreadyRunningException(
      TaskAlreadyRunningException taskAlreadyRunningException) {
    var message = new Message(ErrorCode.TASK_ALREADY_RUNNING.getCode(), taskAlreadyRunningException.getMessage(),
        ErrorCode.TASK_ALREADY_RUNNING.getHelpText());
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
  }
}