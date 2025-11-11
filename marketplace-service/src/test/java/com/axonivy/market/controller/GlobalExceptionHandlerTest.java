package com.axonivy.market.controller;

import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @Test
  void testHandleInvalidZipEntry() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    InvalidZipEntryException exception = new InvalidZipEntryException("Missing required file: README.md");

    ResponseEntity<Map<String, String>> response = handler.handleInvalidZipEntry(exception);

    assertEquals(403, response.getStatusCode().value(), "Should return HTTP 403 Forbidden");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertTrue(response.getBody().get("message").contains("Missing required file: README.md"),
        "Message should include exception detail");
    assertTrue(response.getBody().get("message").contains("Invalid zip entry detected:"),
        "Message should start with expected prefix");
    assertTrue(response.getBody().get("message").endsWith(", skipped this file"),
        "Message should end with expected suffix");
  }
}