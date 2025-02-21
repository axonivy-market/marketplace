package com.axonivy.market.controller;

import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.constants.RequestMappingConstants.BY_ID_AND_VERSION;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.VERSION;

@RestController
@RequestMapping("/hello")
@AllArgsConstructor
public class Test {
  ProductRepository productRepository;
  @GetMapping
  @Operation(summary = "Find product detail by product id and release version.",
      description = "get product detail by it product id and release version")
  public Product findProductDetailsByVersion() {
    Product productDetail = productRepository.findProductById("adobe-acrobat-connector");
    return productDetail;
  }
}
