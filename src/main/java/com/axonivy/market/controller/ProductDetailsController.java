package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {

  @GetMapping("/{id}")
  public ResponseEntity<Object> findProduct(@PathVariable("id") String key,
      @RequestParam(name = "type", required = false) String type) {
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
