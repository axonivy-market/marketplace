package com.axonivy.market.controller;

import static com.axonivy.market.constants.CommonConstants.INITIAL_PAGE;
import static com.axonivy.market.constants.CommonConstants.INITIAL_PAGE_SIZE;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<CollectionModel<EntityModel<Product>>> fetchAllProducts(
      @PathVariable(required = false) String type,
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
      return new ResponseEntity<CollectionModel<EntityModel<Product>>>(HttpStatus.NO_CONTENT);
    }

    List<EntityModel<Product>> productResource = new ArrayList<>();
    for (var product : results) {
      Link activityLink = linkTo(
          methodOn(ProductDetailsController.class).findProduct(product.getKey(), product.getType())).withRel("product");

      Link item = linkTo(methodOn(ProductDetailsController.class).findProduct(product.getKey(), product.getType()))
          .withRel("item");
      items.add(item);
      productResource.add(EntityModel.of(product, activityLink));
    }

    Link self = linkTo(methodOn(ProductController.class).fetchAllProducts(type, sort, page, size)).withSelfRel();
    if (items.size() > 0) {
      links.addAll(items);
    }
    links.add(self);
    return new ResponseEntity<CollectionModel<EntityModel<Product>>>(CollectionModel.of(productResource, links),
        HttpStatus.OK);
  }

}
