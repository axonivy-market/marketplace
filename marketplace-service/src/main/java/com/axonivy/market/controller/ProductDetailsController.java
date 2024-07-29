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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Product Detail Controllers", description = "API collection to get product's detail.")
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
  @Operation(summary = "Find product detail by product id and release tag.", description = "get product detail by it product id and release tag.")
  public ResponseEntity<ProductDetailModel> findProductDetailsByVersion(@PathVariable(ID) @Parameter(name = "Product id (from meta.json)", example = "portal") String id,
                                                                        @PathVariable(TAG) @Parameter(name = "Release tag (from git hub repo tags)", example = "10.0.19") String tag) {
    var productDetail = productService.fetchProductDetail(id);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, tag), HttpStatus.OK);
  }

  @CrossOrigin(originPatterns = "*")
  @PutMapping(INSTALLATION_COUNT_BY_ID)
  @Operation(summary = "Update installation count of product", description = "By default, increase installation count when click download product files by users")
  public ResponseEntity<Integer> syncInstallationCount(@PathVariable(ID) @Parameter(name = "Product id (from meta.json)", example = "portal") String productId) {
    int result = productService.updateInstallationCountForProduct(productId);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  @Operation(summary = "increase installation count by 1", description = "update installation count when click download product files by users")
  public ResponseEntity<ProductDetailModel> findProductDetails(@PathVariable(ID) @Parameter(name = "Product id (from meta.json)", example = "portal") String id) {
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
