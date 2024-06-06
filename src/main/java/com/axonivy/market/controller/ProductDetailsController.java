package com.axonivy.market.controller;

import com.axonivy.market.service.VersionService;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
    private final VersionService service;

    public ProductDetailsController(VersionService service) {
        this.service = service;
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
  @GetMapping("/{key}")
  public Object findProduct(@PathVariable("key") String key,
      @RequestParam(name = "type", required = false) String type) {
    return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
  }
}
