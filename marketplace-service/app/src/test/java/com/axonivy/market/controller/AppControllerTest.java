package com.axonivy.market.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class AppControllerTest {

  @InjectMocks
  private AppController appController;

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