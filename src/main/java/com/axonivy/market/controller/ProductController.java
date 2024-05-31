package com.axonivy.market.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.model.Product;
import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/product")
public class ProductController {

  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @GetMapping()
  public ResponseEntity<String> init() {
    return ResponseEntity.ok("Welcome to /api/product API");
  }

  /**
   * TODO paging with hateoas - unify response handle for /api/product/{type}
   * {type} is all, connector, utils, solution
   **/
  @GetMapping("/{type}")
  public ResponseEntity<List<Product>> fetchAllProducts1(@PathVariable(required = false) String type,
      @RequestParam(required = false, defaultValue = "popularity") String sort) {
    log.warn("Type {} - sort {}", type, sort);
    var results = service.fetchAll(type, sort);
    if (CollectionUtils.isEmpty(results)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(results);
  }
}
