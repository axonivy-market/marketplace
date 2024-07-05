package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {

  @InjectMocks
  private ProductDetailsController productDetailsController;

//  @Test
//  void testFindProduct() {
//    var result = productDetailsController.findProduct("", "");
//    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
//  }

}
