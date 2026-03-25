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
  void shouldReturnBestMatchVersion() {
    String productId = "approval-decision-utils";
    String inputVersion = "10.0.20";
    String bestMatchVersion = "12.0.0";

    when(productService.fetchBestMatchVersion(productId, inputVersion)).thenReturn(bestMatchVersion);
    ResponseEntity<BestMatchVersion> response =
        productDetailsController.findBestMatchProductDetailsByVersion(productId, inputVersion);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Expected HTTP status 200 OK");
    assertNotNull(response.getBody(),
        "Response body should not be null");
    assertEquals(bestMatchVersion, response.getBody().getVersion(),
        "Expected version in response body to match service result");
    verify(productService).fetchBestMatchVersion(productId, inputVersion);
  }
}
