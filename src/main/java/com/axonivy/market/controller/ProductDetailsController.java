package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
  private final ProductService productService;

  public ProductDetailsController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<Object> findProduct(@PathVariable("id") String key,
      @RequestParam(name = "type", required = false) String type) {
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Operation(summary = "increase installation count by 1", description = "increase installation count by 1")
  @PutMapping("/installationcount/{key}")
  public ResponseEntity<Integer> syncInstallationCount(@PathVariable("key") String key) {
    int result = productService.updateInstallationCountForProduct(key);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
