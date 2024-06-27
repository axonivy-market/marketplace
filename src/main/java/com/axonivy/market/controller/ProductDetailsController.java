package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.repository.ProductRepository;
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

  private final ProductRepository productRepository;
  private final ProductModelAssembler assembler;

  public ProductDetailsController(ProductRepository productRepository, ProductModelAssembler assembler) {
    this.productRepository = productRepository;
    this.assembler = assembler;
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductModel> findProduct(@PathVariable("id") String id,
                                                  @RequestParam(name = "type", required = false) String type) {
    Product product = productRepository.findById(id).orElse(null);
    return new ResponseEntity<>(assembler.toModel(product), HttpStatus.OK);
  }
}
