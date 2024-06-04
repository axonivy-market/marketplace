package com.axonivy.market.controller;

import com.axonivy.market.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-details")
public class ProductDetailsController {
    private final ProductService service;

    public ProductDetailsController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{productKey}")
    public Object findProduct(@PathVariable("productKey") String productKey,
                              @RequestParam(name = "type", required = false) String type) {
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{productId}/versions")
    public ResponseEntity<List<String>> fetchAllProducts1(@PathVariable(required = true) String productId,
                                                          @RequestParam(required = false, value = "showDevVersion") Boolean isDevVersionsDisplayed,
                                                          @RequestParam(required = false, value = "designerVersion") String designerVersion) {

        service.getVersions(productId, isDevVersionsDisplayed, designerVersion);
        return new ResponseEntity<>(service.getVersions(productId, isDevVersionsDisplayed, designerVersion), HttpStatus.OK);
    }
}
