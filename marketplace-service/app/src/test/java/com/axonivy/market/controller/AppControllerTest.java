package com.axonivy.market.controller;

import com.axonivy.market.testutil.MockServletRequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppControllerTest {
  private final AppController appController = new AppController();

  @Test
  void testRoot() {
    MockServletRequestUtils.createAndBindMockRequest();
    var response = appController.root();
    MockServletRequestUtils.resetRequestAttributes();

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK for the root endpoint.");
    assertTrue(response.hasBody(),
        "Response should contain a body for the root endpoint.");
    assertTrue(Objects.requireNonNull(response.getBody()).getMessageDetails().contains("/swagger-ui/index.html"),
        "Response body should contain a link to the Swagger UI.");
  }
}
