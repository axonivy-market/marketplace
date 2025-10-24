package com.axonivy.market.controller;

import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidZipEntryException.class)
  public ResponseEntity<Map<String, String>> handleInvalidZipEntry(InvalidZipEntryException ex) {
    Map<String, String> body = new HashMap<>();
    body.put("message", "Invalid zip entry detected: " + ex.getMessage() + ", skipped this file");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

}
