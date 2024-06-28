package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.axonivy.market.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {
  @Mock
  private ProductService productService;
  @InjectMocks
  private ProductDetailsController productDetailsController;

  @Test
  void testFindProduct() {
    var result = productDetailsController.findProduct("", "");
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  public void testSyncInstallationCount() throws Exception {
    // prepare
    when(productService.updateInstallationCountForProduct("google-maps-connector")).thenReturn(1);

    // exercise
    var result = productDetailsController.syncInstallationCount("google-maps-connector");

    // Verify the interaction with the mock
    assertEquals(1, result.getBody());
  }
}
