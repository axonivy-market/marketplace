package com.axonivy.market.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.axonivy.market.model.Message;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

  private static final String NOT_FOUND_EXCEPTION_CODE = "-1";

  @ExceptionHandler(MissingHeaderException.class)
  protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingHeaderException missingHeaderException) {
    var errorMessage = new Message();
    errorMessage.setMessageDetails(missingHeaderException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<Object> handleNotFoundException(NotFoundException notFoundException) {
    var errorMessage = new Message();
    errorMessage.setErrorCode(NOT_FOUND_EXCEPTION_CODE);
    errorMessage.setMessageDetails(notFoundException.getMessage());
    return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
  }
}
