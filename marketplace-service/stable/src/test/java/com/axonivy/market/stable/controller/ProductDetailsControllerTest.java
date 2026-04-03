package com.axonivy.market.stable.controller;

import com.axonivy.market.stable.model.BestMatchVersion;
import com.axonivy.market.stable.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductDetailsControllerTest {

  @Mock
  private ProductServiceImpl productService;

  @InjectMocks
  private ProductDetailsController productDetailsController;

  @Test
  void testGetBestMatchVersion() {
    String id = "product-1";
    String version = "1.0";
    Boolean showDev = false;

    when(productService.getBestMatchVersion(id, version, showDev)).thenReturn("2.0");

    ResponseEntity<BestMatchVersion> response =
        productDetailsController.findBestMatchProductDetailsByVersion(id, version, showDev);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Expected HTTP status 200 OK");
    assertNotNull(response.getBody(),
        "Response body should not be null");
    assertEquals("2.0", response.getBody().getVersion(),
        "Expected version in response body to match service result");
    verify(productService).getBestMatchVersion(id, version, showDev);
  }

  @Test
  void shouldPassShowDevVersionTrue() {
    String id = "product-1";
    String version = "1.0";

    when(productService.getBestMatchVersion(id, version, true)).thenReturn("2.1-dev");

    ResponseEntity<BestMatchVersion> response =
        productDetailsController.findBestMatchProductDetailsByVersion(id, version, true);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Expected HTTP status 200 OK");
    assertEquals("2.1-dev", response.getBody().getVersion(),
        "Expected dev version to be returned");
    verify(productService).getBestMatchVersion(id, version, true);
  }
}
