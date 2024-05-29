package com.axonivy.market.exceptions;

import com.axonivy.market.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MissingHeaderException.class)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingHeaderException missingHeaderException) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, missingHeaderException.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
