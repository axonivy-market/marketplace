package com.axonivy.market.stable.controller;

import static com.axonivy.market.core.constants.CoreRequestParamConstants.DESIGNER_VERSION;
import static com.axonivy.market.core.constants.CoreRequestParamConstants.ID;

import static com.axonivy.market.stable.constants.RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION;
import static com.axonivy.market.stable.constants.RequestMappingConstants.PRODUCT_DETAILS;

import com.axonivy.market.stable.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import lombok.AllArgsConstructor;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
  private final VersionService versionService;

  @GetMapping(BEST_MATCH_BY_ID_AND_VERSION)
  @Operation(summary = "Find best match product detail by product id and version.",
      description = "get product detail by it product id and version")
  public ResponseEntity<ProductDetailModel> findBestMatchProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Version", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchBestMatchProductDetail(id, version);
    ProductDetailModel model = detailModelAssembler.toModel(productDetail);
    var findProductJsonLink = methodOn(this.getClass()).findProductJsonContent(id,
        productDetail.getBestMatchVersion(), version);
    model.setMetaProductJsonUrl(linkTo(findProductJsonLink).withSelfRel().getHref());
    var findBestMatchLink = methodOn(this.getClass()).findBestMatchProductDetailsByVersion(id, version);
    model.add(linkTo(findBestMatchLink).withSelfRel());
    return new ResponseEntity<>(model, HttpStatus.OK);
  }

  @GetMapping(PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION)
  @TrackApiCallFromNeo
  @Operation(summary = "Get product json content for designer to install",
      description = "When we click install in designer, this API will send content of product json for installing in " +
          "Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(ID) String productId,
      @PathVariable(VERSION) String version,
      @RequestParam(name = DESIGNER_VERSION, required = false) String designerVersion) {
    Map<String, Object> productJsonContent = versionService.getProductJsonContentByIdAndVersion(productId, version,
        designerVersion);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }
}
