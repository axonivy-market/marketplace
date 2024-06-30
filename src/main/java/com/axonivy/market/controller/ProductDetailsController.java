package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {

    private final ProductService service;
    private final ProductDetailModelAssembler detailModelAssembler;

    public ProductDetailsController(ProductService service, ProductDetailModelAssembler detailModelAssembler) {
        this.service = service;
        this.detailModelAssembler = detailModelAssembler;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailModel> findProductDetails(@PathVariable("id") String id,
                                                                 @RequestParam(name = "type") String type) {
        var productDetail = service.fetchProductDetail(id, type);
        return new ResponseEntity<>(detailModelAssembler.toModel(productDetail), HttpStatus.OK);
    }
}
