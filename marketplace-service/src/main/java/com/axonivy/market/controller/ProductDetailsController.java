package com.axonivy.market.controller;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

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

  @GetMapping("/{id}/{version}")
  public ResponseEntity<ProductDetailModel> findProductDetailsByVersion(@PathVariable("id") String id,
      @PathVariable("version") String version) {
    var productDetail = productService.fetchProductDetail(id);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version), HttpStatus.OK);
  }

  @Operation(summary = "increase installation count by 1", description = "update installation count when click download product files by users")
  @CrossOrigin(originPatterns = "*")
  @PutMapping("/installationcount/{key}")
  public ResponseEntity<Integer> syncInstallationCount(@PathVariable("key") String key) {
    int result = productService.updateInstallationCountForProduct(key);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDetailModel> findProductDetails(@PathVariable("id") String id) {
    var productDetail = productService.fetchProductDetail(id);
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, null), HttpStatus.OK);
  }

  @GetMapping("/{id}/versions")
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(@PathVariable("id") String id,
      @RequestParam(name = "isShowDevVersion") boolean isShowDevVersion,
      @RequestParam(name = "designerVersion", required = false) String designerVersion) {
    List<MavenArtifactVersionModel> models =
        versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }
}
