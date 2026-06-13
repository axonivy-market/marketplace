package com.axonivy.market.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppControllerTest {
  private final AppController appController = new AppController();

  @Test
  void testRoot() {
    var response = appController.root();

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK for the root endpoint.");
    assertTrue(response.hasBody(),
        "Response should contain a body for the root endpoint.");
    assertTrue(Objects.requireNonNull(response.getBody()).getMessageDetails().contains("/swagger-ui/index.html"),
        "Response body should contain a link to the Swagger UI.");
  }
}
