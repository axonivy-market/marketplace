package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.service.ProductService;
import org.springframework.hateoas.EntityModel;
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

  private final ProductService service;
  private final ProductDetailModelAssembler detailModelAssembler;

  private final GHAxonIvyProductRepoService ghAxonIvyProductRepoService;

  public ProductDetailsController(ProductService service, ProductDetailModelAssembler detailModelAssembler, GHAxonIvyProductRepoService ghAxonIvyProductRepoService) {
    this.service = service;
    this.detailModelAssembler = detailModelAssembler;
    this.ghAxonIvyProductRepoService = ghAxonIvyProductRepoService;
  }

  @GetMapping("/{key}")
  public Object findProduct(@PathVariable("key") String key,
                            @RequestParam(name = "type", required = false) String type) {
    var productDetailModel = service.fetch(key);

//    return new ResponseEntity<Object>(EntityModel.of(detailModelAssembler.toModel(product)),HttpStatus.OK);
    return new ResponseEntity<Object>(EntityModel.of(productDetailModel),HttpStatus.OK);
  }

//  @GetMapping()
//  public GHContent methodName(){
//
//  }
}
