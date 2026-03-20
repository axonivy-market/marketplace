package com.axonivy.market.stable.controller;

import com.axonivy.market.stable.model.BestMatchVersion;
import com.axonivy.market.stable.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.core.constants.CoreRequestParamConstants.ID;
import static com.axonivy.market.core.constants.CoreRequestParamConstants.VERSION;
import static com.axonivy.market.stable.constants.RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION;
import static com.axonivy.market.stable.constants.RequestMappingConstants.PRODUCT_DETAILS;

@Log4j2
@RestController
@RequestMapping(value = PRODUCT_DETAILS, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ProductDetailsController {
  private final ProductService productService;

  @GetMapping(BEST_MATCH_BY_ID_AND_VERSION)
  @Operation(summary = "Find best match version of a product by product id and version.",
      description = "Get best match version by product id and version")
  public ResponseEntity<BestMatchVersion> findBestMatchProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Version", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var bestMatchVersion = productService.fetchBestMatchVersion(id, version);
    var result = new BestMatchVersion(bestMatchVersion);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
