package com.axonivy.market.core.controller;

import com.axonivy.market.core.model.ProductDetailModel;
import com.axonivy.market.core.service.CoreProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION;
import static com.axonivy.market.core.constants.CoreRequestMappingConstants.PRODUCT_DETAILS;
import static com.axonivy.market.core.constants.CoreRequestParamConstants.ID;
import static com.axonivy.market.core.constants.CoreRequestParamConstants.VERSION;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@AllArgsConstructor
@RestController
@RequestMapping(PRODUCT_DETAILS)
@Tag(name = "Product Detail Controllers", description = "API collection to get product's detail.")
public class CoreProductDetailsController {
  private final CoreProductService coreProductService;

  @GetMapping(BEST_MATCH_BY_ID_AND_VERSION)
  @Operation(summary = "Find best match product detail by product id and version.",
      description = "get product detail by it product id and version")
  public ResponseEntity<ProductDetailModel> findBestMatchProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Version", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var productDetail = coreProductService.fetchBestMatchProductDetail(id, version);
    ProductDetailModel model = detailModelAssembler.toModel(productDetail);
    var findProductJsonLink = methodOn(this.getClass()).findProductJsonContent(id,
        productDetail.getBestMatchVersion(), version);
    model.setMetaProductJsonUrl(linkTo(findProductJsonLink).withSelfRel().getHref());
    var findBestMatchLink = methodOn(this.getClass()).findBestMatchProductDetailsByVersion(id, version);
    model.add(linkTo(findBestMatchLink).withSelfRel());
    return new ResponseEntity<>(model, HttpStatus.OK);
  }
}
