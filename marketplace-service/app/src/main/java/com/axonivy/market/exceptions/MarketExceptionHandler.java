package com.axonivy.market.exceptions;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.InvalidParamException;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.exceptions.model.ArchiveNotAllowedException;
import com.axonivy.market.exceptions.model.UnarchiveFailedException;
import com.axonivy.market.exceptions.model.FileProcessingException;
import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.SyncTaskInProgressException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.model.Message;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MarketExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException exception) {
    return messageResponse(HttpStatus.BAD_REQUEST, exception.getDetailMessageCode(), exception.getMessage());
  }

  @ExceptionHandler(MarketException.class)
  public ResponseEntity<Object> handleAMarketException(MarketException marketException) {
    return messageResponse(HttpStatus.BAD_REQUEST, marketException.getCode(), marketException.getMessage());
  }

  @ExceptionHandler(AlreadyExistedException.class)
  public ResponseEntity<Object> handleAlreadyExistedException(AlreadyExistedException alreadyExistedException) {
    return messageResponse(HttpStatus.BAD_REQUEST, alreadyExistedException.getCode(),
        alreadyExistedException.getMessage());
  }

  @ExceptionHandler(MissingHeaderException.class)
  public ResponseEntity<Object> handleMissingServletRequestParameter(Throwable missingHeaderException) {
    return messageResponse(HttpStatus.BAD_REQUEST, null, missingHeaderException.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Object> handleNotFoundException(NotFoundException notFoundException) {
    return messageResponse(HttpStatus.NOT_FOUND, notFoundException.getCode(), notFoundException.getMessage());
  }

  @ExceptionHandler(NoContentException.class)
  public ResponseEntity<Object> handleNoContentException(NoContentException noContentException) {
    return messageResponse(HttpStatus.NO_CONTENT, noContentException.getCode(), noContentException.getMessage());
  }

  @ExceptionHandler(InvalidParamException.class)
  public ResponseEntity<Object> handleInvalidException(InvalidParamException invalidDataException) {
    return messageResponse(HttpStatus.BAD_REQUEST, invalidDataException.getCode(), invalidDataException.getMessage());
  }

  @ExceptionHandler(Oauth2ExchangeCodeException.class)
  public ResponseEntity<Object> handleOauth2ExchangeCodeException(
      Oauth2ExchangeCodeException oauth2ExchangeCodeException) {
    return messageResponse(HttpStatus.BAD_REQUEST, oauth2ExchangeCodeException.getError(),
        oauth2ExchangeCodeException.getErrorDescription());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorizedException(
      UnauthorizedException unauthorizedException) {
    return messageResponse(HttpStatus.UNAUTHORIZED, unauthorizedException.getError(),
        unauthorizedException.getMessage());
  }

  @ExceptionHandler(InvalidZipEntryException.class)
  public ResponseEntity<Map<String, String>> handleInvalidZipEntry(InvalidZipEntryException ex) {
    Map<String, String> body = new HashMap<>();
    body.put("message", "Invalid zip entry detected: " + ex.getMessage() + ", skipped this file");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(FileProcessingException.class)
  public ResponseEntity<Object> handleFileProcess(FileProcessingException fileProcessingException) {
    return messageResponse(HttpStatus.BAD_REQUEST, fileProcessingException.getCode(),
        fileProcessingException.getMessage());
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<?> handleIOException(IOException ex, HttpServletRequest request) {
    var responseBuilder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
    if (isServerSentEventRequest(request)) {
      return responseBuilder.build();
    }
    var message = new Message(ErrorCode.INTERNAL_EXCEPTION.getCode(),
        ErrorCode.INTERNAL_EXCEPTION.getHelpText(), ex.getMessage());
    return responseBuilder.body(message);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Message> handleIllegalArgumentException(IllegalArgumentException ex) {
    var message = new Message(ErrorCode.ARGUMENT_BAD_REQUEST.getCode(), ex.getMessage(),
        ErrorCode.ARGUMENT_BAD_REQUEST.getHelpText());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
  }

  @ExceptionHandler(SyncTaskInProgressException.class)
  public ResponseEntity<Message> handleSyncTaskInProgressException(
      SyncTaskInProgressException syncTaskInProgressException) {
    var message = new Message(ErrorCode.TASK_ALREADY_IN_PROGRESS.getCode(), syncTaskInProgressException.getMessage(),
        ErrorCode.TASK_ALREADY_IN_PROGRESS.getHelpText());
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Message> handleResponseStatusException(ResponseStatusException exception) {
    var message = new Message(String.valueOf(exception.getStatusCode().value()), exception.getReason(),
        exception.getReason());
    return ResponseEntity.status(exception.getStatusCode()).body(message);
  }

  @ExceptionHandler(ArchiveNotAllowedException.class)
  public ResponseEntity<Object> handleArchiveNotAllowedException(
      ArchiveNotAllowedException archiveNotAllowedException) {
    return messageResponse(HttpStatus.BAD_REQUEST, archiveNotAllowedException.getCode(),
        archiveNotAllowedException.getMessage());
  }

  @ExceptionHandler(UnarchiveFailedException.class)
  public ResponseEntity<Object> handleUnarchiveFailedException(
      UnarchiveFailedException unarchiveFailedException) {
    return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, unarchiveFailedException.getCode(),
        unarchiveFailedException.getMessage());
  }

  private boolean isServerSentEventRequest(HttpServletRequest request) {
    return request != null
        && request.getHeader(HttpHeaders.ACCEPT) != null
        && request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.TEXT_EVENT_STREAM_VALUE);
  }

  private ResponseEntity<Object> messageResponse(HttpStatus status, String helpCode, String messageDetails) {
    Message errorMessage = new Message();
    errorMessage.setHelpCode(helpCode);
    errorMessage.setMessageDetails(messageDetails);
    return new ResponseEntity<>(errorMessage, status);
  }
}
