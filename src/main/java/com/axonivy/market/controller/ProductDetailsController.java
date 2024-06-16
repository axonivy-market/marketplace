package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
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

    private final GHAxonIvyProductRepoService ghAxonIvyProductRepoService;
    private final ProductDetailModelAssembler detailModelAssembler;

    public ProductDetailsController(ProductService service, ProductDetailModelAssembler detailModelAssembler, GHAxonIvyProductRepoService ghAxonIvyProductRepoService, GHAxonIvyProductRepoService ghAxonIvyProductRepoService1) {
        this.service = service;
        this.detailModelAssembler = detailModelAssembler;
        this.ghAxonIvyProductRepoService = ghAxonIvyProductRepoService1;
    }

    @GetMapping("/{key}")
    public ResponseEntity<ProductDetailModel> findProduct(@PathVariable("key") String key,
                                                          @RequestParam(name = "type", required = false) String type) {
        var productDetail = service.fetchProductDetail(key);
        return new ResponseEntity<>(detailModelAssembler.toModel(productDetail), HttpStatus.OK);
    }

    @GetMapping("/{key}/readme")
    public ResponseEntity<ReadmeModel> getReadmeContentOfProductTag(@PathVariable("key") String key, @RequestParam(name = "tag") String tag) {
        var readme = service.getReadmeContent(key, tag);
        return new ResponseEntity<>(readme, HttpStatus.OK);
    }
}
