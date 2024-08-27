package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION;
import static com.axonivy.market.constants.RequestMappingConstants.VERSIONS_IN_DESIGNER;
import static com.axonivy.market.constants.RequestParamConstants.DESIGNER_VERSION;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.PRODUCT_ID;
import static com.axonivy.market.constants.RequestParamConstants.SHOW_DEV_VERSION;
import static com.axonivy.market.constants.RequestParamConstants.VERSION;
import static com.axonivy.market.constants.RequestMappingConstants.BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.BY_ID_AND_VERSION;
import static com.axonivy.market.constants.RequestMappingConstants.INSTALLATION_COUNT_BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;
import static com.axonivy.market.constants.RequestMappingConstants.VERSIONS_BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

  public ProductDetailsController(VersionService versionService, ProductService productService, ProductDetailModelAssembler detailModelAssembler) {
    this.versionService = versionService;
    this.productService = productService;
    this.detailModelAssembler = detailModelAssembler;
  }

  @GetMapping(BY_ID_AND_VERSION)
  @Operation(summary = "Find product detail by product id and release version.", description = "get product detail by it product id and release version")
  public ResponseEntity<ProductDetailModel> findProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils", in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Release version (from maven metadata.xml)", example = "10.0.20", in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchProductDetailByIdAndVersion(id, version);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version, BY_ID_AND_VERSION), HttpStatus.OK);
  }

  @GetMapping(BEST_MATCH_BY_ID_AND_VERSION)
  @Operation(summary = "Find best match product detail by product id and version.", description = "get product detail by it product id and version")
  public ResponseEntity<ProductDetailModel> findBestMatchProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils", in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Version", example = "10.0.20", in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchBestMatchProductDetail(id,version);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version, BEST_MATCH_BY_ID_AND_VERSION), HttpStatus.OK);
  }

  @CrossOrigin(originPatterns = "*")
  @PutMapping(INSTALLATION_COUNT_BY_ID)
  @Operation(summary = "Update installation count of product", description = "By default, increase installation count when click download product files by users")
  public ResponseEntity<Integer> syncInstallationCount(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils", in = ParameterIn.PATH) String productId) {
    int result = productService.updateInstallationCountForProduct(productId);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  @Operation(summary = "increase installation count by 1", description = "update installation count when click download product files by users")
  public ResponseEntity<ProductDetailModel> findProductDetails(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils", in = ParameterIn.PATH) String id) {
    var productDetail = productService.fetchProductDetail(id);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, BY_ID), HttpStatus.OK);
  }

  @GetMapping(VERSIONS_BY_ID)
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "adobe-acrobat-connector", in = ParameterIn.PATH) String id,
      @RequestParam(SHOW_DEV_VERSION) @Parameter(description = "Option to get Dev Version (Snapshot/ sprint release)", in = ParameterIn.QUERY) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY, example = "v10.0.20") String designerVersion) {
    List<MavenArtifactVersionModel> models =
        versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }

  @GetMapping(PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION)
  @Operation(summary = "Get product json content for designer to install", description = "When we click install in designer, this API will send content of product json for installing in Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(PRODUCT_ID) String productId,
      @PathVariable(VERSION) String version) throws JsonProcessingException {
    Map<String, Object> productJsonContent = versionService.getProductJsonContentByIdAndVersion(productId, version);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }

  @GetMapping(VERSIONS_IN_DESIGNER)
  @Operation(summary = "Get the list of released version in product", description = "Collect the released versions in product for ivy designer")
  public ResponseEntity<List<String>> findVersionsForDesigner(@PathVariable(ID) String id,
      @RequestParam(SHOW_DEV_VERSION) @Parameter(description = "Option to get Dev Version (Snapshot/ sprint release)", in = ParameterIn.QUERY) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY, example = "v10.0.20") String designerVersion) {
    List<String> versionList = versionService.getVersionsForDesigner(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(versionList, HttpStatus.OK);
  }

}
