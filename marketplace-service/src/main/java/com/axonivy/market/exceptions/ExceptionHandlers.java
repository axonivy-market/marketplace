package com.axonivy.market.exceptions;

import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.model.Message;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {
    BindingResult bindingResult = ex.getBindingResult();
    List<String> errors = new ArrayList<>();
    if (bindingResult.hasErrors()) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        errors.add(fieldError.getDefaultMessage());
      }
    } else {
      errors.add(ex.getMessage());
    }

    var errorMessage = new Message();
    errorMessage.setHelpCode(ErrorCode.ARGUMENT_BAD_REQUEST.getCode());
    errorMessage.setMessageDetails(ErrorCode.ARGUMENT_BAD_REQUEST.getHelpText() + " - " + String.join("; ", errors));
    return new ResponseEntity<>(errorMessage, status);
  }

  @ExceptionHandler(MissingHeaderException.class)
  public ResponseEntity<Object> handleMissingServletRequestParameter(MissingHeaderException missingHeaderException) {
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

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
    return new ResponseEntity<>("Invalid URL", HttpStatus.NOT_FOUND);
  }
}
