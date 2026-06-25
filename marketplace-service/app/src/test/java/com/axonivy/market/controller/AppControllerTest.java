package com.axonivy.market.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppControllerTest {

  private final AppController appController = new AppController();

  @Test
  void testRoot() {
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    var response = appController.root();
    RequestContextHolder.resetRequestAttributes();

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK for the root endpoint.");
    assertTrue(response.hasBody(),
        "Response should contain a body for the root endpoint.");
    assertTrue(Objects.requireNonNull(response.getBody()).getMessageDetails().contains("/swagger-ui/index.html"),
        "Response body should contain a link to the Swagger UI.");
  }
}
