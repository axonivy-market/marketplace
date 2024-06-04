package com.axonivy.market.controller;

import static com.axonivy.market.constants.CommonConstants.INITIAL_PAGE;
import static com.axonivy.market.constants.CommonConstants.INITIAL_PAGE_SIZE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/product")
public class ProductController {

  private final ProductService service;
  private final ProductModelAssembler assembler;

  public ProductController(ProductService service, ProductModelAssembler assembler) {
    this.service = service;
    this.assembler = assembler;
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
  public ResponseEntity<CollectionModel<ProductModel>> fetchAllProducts(@PathVariable(required = false) String type,
      @RequestParam(required = false, defaultValue = "popularity") String sort,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size) {
    log.warn("Type {} - sort {}", type, sort);
    List<Link> links = new ArrayList<Link>();
    List<Link> items = new ArrayList<Link>();
    int evalPageSize = size == null || size < 1 ? INITIAL_PAGE_SIZE : size;
    int evalPage = page == null || page < 1 ? INITIAL_PAGE : page;

    var results = service.fetchAll(type, sort, evalPage, evalPageSize);
    if (CollectionUtils.isEmpty(results)) {
      return new ResponseEntity<CollectionModel<ProductModel>>(HttpStatus.NO_CONTENT);
    }

    List<ProductModel> productResource = new ArrayList<>();
    for (var product : results) {
      productResource.add(assembler.toModel(product));
    }

    Link self = linkTo(methodOn(ProductController.class).fetchAllProducts(type, sort, page, size)).withSelfRel();
    if (items.size() > 0) {
      links.addAll(items);
    }
    links.add(self);
    return new ResponseEntity<CollectionModel<ProductModel>>(CollectionModel.of(productResource), HttpStatus.OK);
  }

  @GetMapping("/{productId}/versions")
  public ResponseEntity<List<String>> fetchAllProducts1(@PathVariable(required = true) String productId) {
    log.warn("product id {}",productId);
    var results = service.getVersions(productId);
    if (CollectionUtils.isEmpty(results)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(results);
  }
}
