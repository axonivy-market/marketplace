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
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertTrue(Objects.requireNonNull(response.getBody()).getMessageDetails().contains("/swagger-ui/index.html"));
  }
}