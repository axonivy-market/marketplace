package com.axonivy.market.controller;

import com.axonivy.market.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

@Log4j2
@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
    private final VersionService service;

    public ProductDetailsController(VersionService service) {
        this.service = service;
    }


    @GetMapping("/{id}")
  public ResponseEntity<Object> findProduct(@PathVariable("id") String key,
      @RequestParam(name = "type", required = false) String type) {
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
