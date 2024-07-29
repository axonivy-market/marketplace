package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.BY_ID_AND_TAG;
import static com.axonivy.market.constants.RequestMappingConstants.INSTALLATION_COUNT_BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;
import static com.axonivy.market.constants.RequestMappingConstants.VERSIONS_BY_ID;
import static com.axonivy.market.constants.RequestParamConstants.DESIGNER_VERSION;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.SHOW_DEV_VERSION;
import static com.axonivy.market.constants.RequestParamConstants.TAG;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
  private final VersionService versionService;
  private final ProductService productService;
  private final ProductDetailModelAssembler detailModelAssembler;

  public ProductDetailsController(VersionService versionService, ProductService productService,
      ProductDetailModelAssembler detailModelAssembler) {
    this.versionService = versionService;
    this.productService = productService;
    this.detailModelAssembler = detailModelAssembler;
  }

  @GetMapping(BY_ID_AND_TAG)
  public ResponseEntity<ProductDetailModel> findProductDetailsByVersion(@PathVariable(ID) String id,
      @PathVariable(TAG) String tag) {
    var productDetail = productService.fetchProductDetail(id);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, tag), HttpStatus.OK);
  }

  @Operation(summary = "increase installation count by 1", description = "update installation count when click download product files by users")
  @CrossOrigin(originPatterns = "*")
  @PutMapping(INSTALLATION_COUNT_BY_ID)
  public ResponseEntity<Integer> syncInstallationCount(@PathVariable(ID) String key) {
    int result = productService.updateInstallationCountForProduct(key);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  public ResponseEntity<ProductDetailModel> findProductDetails(@PathVariable(ID) String id) {
    var productDetail = productService.fetchProductDetail(id);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, null), HttpStatus.OK);
  }

  @GetMapping(VERSIONS_BY_ID)
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(@PathVariable(ID) String id,
      @RequestParam(SHOW_DEV_VERSION) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) String designerVersion) {
    List<MavenArtifactVersionModel> models =
        versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }
}
