package com.axonivy.market.neo.controller;

import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.service.CoreVersionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.axonivy.market.core.constants.CoreRequestParamConstants.*;
import static com.axonivy.market.neo.constants.RequestMappingConstants.PRODUCT;
import static com.axonivy.market.neo.constants.RequestMappingConstants.VERSIONS_BY_ID;
import static com.axonivy.market.neo.constants.RequestMappingConstants.PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION;

@RestController
@RequestMapping(PRODUCT)
@AllArgsConstructor
@Tag(name = "Product Controller", description = "API collection to get and search products")
public class ProductController {
  private CoreVersionService versionService;

  @GetMapping(PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION)
  @Operation(summary = "Get product json content for designer to install",
      description = "When we click install in designer, this API will send content of product json for installing in Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(ID) String productId,
      @RequestParam(name = DESIGNER_VERSION, required = false) String designerVersion) {
    Map<String, Object> productJsonContent = versionService.getProductJsonContentByIdAndVersion(productId, designerVersion);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }

  @GetMapping(VERSIONS_BY_ID)
  @Operation(summary = "Get product versions by product id",
      description = "Get all product versions by product id")
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "adobe-acrobat-connector",
          in = ParameterIn.PATH) String id,
      @RequestParam(name = SHOW_DEV_VERSION, required = false) @Parameter(description = "Option to get Dev Version " +
          "(Snapshot/ sprint release)",
          in = ParameterIn.QUERY) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY,
          example = "v10.0.20") String designerVersion) {
    List<MavenArtifactVersionModel> models =
        versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }
}
