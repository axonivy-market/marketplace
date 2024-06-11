package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.ProductDetailModel;
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

    public ProductDetailsController(ProductService service, ProductDetailModelAssembler detailModelAssembler, GHAxonIvyProductRepoService ghAxonIvyProductRepoService) {
        this.service = service;
        this.detailModelAssembler = detailModelAssembler;
    }

    @GetMapping("/{key}")
    public ResponseEntity<ProductDetailModel> findProduct(@PathVariable("key") String key,
                                                          @RequestParam(name = "type", required = false) String type) {
        var productDetail = service.fetchProductDetail(key);
        return new ResponseEntity<>(detailModelAssembler.toModel(productDetail), HttpStatus.OK);
    }
}
