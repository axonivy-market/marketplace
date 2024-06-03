package com.axonivy.market.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-details")
public class ProductDetailsController {

  @GetMapping("/{productKey}")
  public Object findProduct(@PathVariable("productKey") String productKey,
      @RequestParam(name = "type", required = false) String type) {
    return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
  }
}
