package com.axonivy.market.controller;

import com.axonivy.market.service.VersionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-details")
public class ProductDetailsController {
    private final VersionService service;

    public ProductDetailsController(VersionService service) {
        this.service = service;
    }

    @GetMapping("/{productKey}")
    public Object findProduct(@PathVariable("productKey") String productKey,
                              @RequestParam(name = "type", required = false) String type) {
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{productId}/versions")
    public ResponseEntity<List<String>> fecthAllVersionFromProduct(@PathVariable(required = true) String productId,
                                                          @RequestParam(required = false, value = "showDevVersion") Boolean isDevVersionsDisplayed,
                                                          @RequestParam(required = false, value = "designerVersion") String designerVersion) {
        return new ResponseEntity<>(service.getVersionsToDisplay(productId, isDevVersionsDisplayed, designerVersion), HttpStatus.OK);
    }

    @GetMapping("/{productId}/artifacts")
    public ResponseEntity<Map<String, List<String>>> fetchAllArtifactsFromProduct(@PathVariable(required = true) String productId){
        return new ResponseEntity<>(service.getArtifactsToDisplay(productId), HttpStatus.OK);
    }
}
