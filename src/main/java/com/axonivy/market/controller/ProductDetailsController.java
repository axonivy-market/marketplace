package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.ReadmeModel;
import com.axonivy.market.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                                                 @RequestParam(name = "type", required = false) String type) {
        var productDetail = service.fetchProductDetail(id, type);
        return new ResponseEntity<>(detailModelAssembler.toModel(productDetail), HttpStatus.OK);
    }

    @GetMapping("/{id}/readme")
    public ResponseEntity<ReadmeModel> getReadmeAndProductContentsFromTag(@PathVariable("id") String id, @RequestParam(name = "tag") String tag) {
        var readme = service.getReadmeAndProductContentsFromTag(id, tag);
        return new ResponseEntity<>(readme, HttpStatus.OK);
    }
}
