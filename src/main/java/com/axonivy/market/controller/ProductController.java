package com.axonivy.market.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.Message;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductController {

  private final ProductService service;
  private final ProductModelAssembler assembler;
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  public ProductController(ProductService service, ProductModelAssembler assembler,
      PagedResourcesAssembler<Product> pagedResourcesAssembler) {
    this.service = service;
    this.assembler = assembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @GetMapping()
  public ResponseEntity<Message> init() {
    var message = new Message();
    message.setMessage("Marketplace product apis");
    return new ResponseEntity<Message>(message, HttpStatus.OK);
  }

  @GetMapping("/{type}")
  public ResponseEntity<PagedModel<ProductModel>> fetchAllProducts(@PathVariable(required = false) String type,
      Pageable pageable) {
    var results = service.fetchAll(type, pageable);
    if (results.isEmpty()) {
      return new ResponseEntity<>(PagedModel.empty(), HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(pagedResourcesAssembler.toModel(results, assembler), HttpStatus.OK);
  }
}
