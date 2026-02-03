package com.axonivy.market.stable.controller;

import com.axonivy.market.stable.assembler.ProductModelAssembler;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.model.ProductModel;
import com.axonivy.market.core.service.CoreProductService;
import com.axonivy.market.stable.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
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
import static com.axonivy.market.stable.constants.RequestMappingConstants.PRODUCT;
import static com.axonivy.market.stable.constants.RequestMappingConstants.VERSIONS_BY_ID;
import static com.axonivy.market.stable.constants.RequestMappingConstants.PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION;

@Log4j2
@RestController
@RequestMapping(PRODUCT)
@AllArgsConstructor
@Tag(name = "Product Controller", description = "API collection to get and search products")
public class ProductController {
  private final VersionService versionService;
  private final CoreProductService coreProductService;
  private final ProductModelAssembler assembler;
  private final PagedResourcesAssembler<Product> pagedResourcesAssembler;


  @GetMapping(PRODUCT_JSON_CONTENT_BY_ID_AND_VERSION)
  @Operation(summary = "Get product json content for designer to install",
      description = "When we click install in designer, this API will send content of product json for installing in "
          + "Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(ID) @Parameter(description =
          "Product id (from meta.json)", example = "connectivity-demo")String productId,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY,
          example = "13.2.0") String designerVersion) {
    Map<String, Object> productJsonContent = versionService.getProductJsonContentByIdAndVersion(productId,
        designerVersion);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }

  @GetMapping(VERSIONS_BY_ID)
  @Operation(summary = "Get product versions by product id", description = "Get all product versions by product id")
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "connectivity-demo",
          in = ParameterIn.PATH) String id,
      @RequestParam(name = SHOW_DEV_VERSION, required = false) @Parameter(description = "Option to get Dev Version "
          + "(Snapshot/ sprint release)", in = ParameterIn.QUERY) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY,
          example = "10.0.20") String designerVersion) {
    List<MavenArtifactVersionModel> models = versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion,
        designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }

  @GetMapping()
  @Operation(summary = "Retrieve a paginated list of all products, optionally filtered by type, keyword, and language",
      description = "By default, the system finds products with type 'all'",
      parameters = {@Parameter(name = "page", description = "Page number to retrieve", in = ParameterIn.QUERY,
          example = "0"), @Parameter(name = "size", description = "Number of items per page", in = ParameterIn.QUERY,
          example = "20"), @Parameter(name = "sort",
          description = "Sorting criteria in the format: Sorting criteria(popularity|alphabetically|recent), Sorting "
              + "order(asc|desc)",
          in = ParameterIn.QUERY, example = "[\"popularity\",\"asc\"]")})
  public ResponseEntity<PagedModel<ProductModel>> findProducts(
      @RequestParam(name = TYPE, required = false) @Parameter(description = "Type of product.", in = ParameterIn.QUERY,
          schema = @Schema(type = "string",
              allowableValues = {"all", "connectors", "utilities", "solutions", "demos"})) String type,
      @RequestParam(name = KEYWORD, required = false) @Parameter(
          description = "Keyword that exist in product's name or short description", example = "connector",
          in = ParameterIn.QUERY) String keyword,
      @RequestParam(name = LANGUAGE, required = false) @Parameter(description = "Language of product short description",
          in = ParameterIn.QUERY, schema = @Schema(allowableValues = {"en", "de"})) String language,
      @PageableDefault(size = Integer.MAX_VALUE) @ParameterObject Pageable pageable) {

    Page<Product> results = coreProductService.findProducts(type, keyword, language, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var pageResources = pagedResourcesAssembler.toModel(results, assembler);
    return ResponseEntity.ok(pageResources);
  }

  private ResponseEntity<PagedModel<ProductModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<ProductModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        ProductModel.class);
    return ResponseEntity.ok(emptyPagedModel);
  }
}
