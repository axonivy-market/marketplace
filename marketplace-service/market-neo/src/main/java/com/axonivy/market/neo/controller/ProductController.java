package com.axonivy.market.neo.controller;

import com.axonivy.market.core.service.CoreVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.axonivy.market.core.constants.RequestParamConstants.*;
import static com.axonivy.market.neo.constants.RequestMappingConstants.PRODUCT;
import static com.axonivy.market.neo.constants.RequestMappingConstants.PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION;

@RestController
@RequestMapping(PRODUCT)
@AllArgsConstructor
@Tag(name = "Product Controller", description = "API collection to get and search products")
public class ProductController {
  private CoreVersionService service;

  @GetMapping(PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION)
  @Operation(summary = "Get product json content for designer to install",
      description = "When we click install in designer, this API will send content of product json for installing in " +
          "Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(ID) String productId,
      @PathVariable(VERSION) String version,
      @RequestParam(name = DESIGNER_VERSION, required = false) String designerVersion) {
    Map<String, Object> productJsonContent = service.getProductJsonContentByIdAndVersion(productId, version,
        designerVersion);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }
}
