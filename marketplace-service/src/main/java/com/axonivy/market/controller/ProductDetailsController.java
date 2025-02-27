package com.axonivy.market.controller;

import com.axonivy.market.assembler.GithubReleaseModelAssembler;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.model.GithubReleaseModel;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.axonivy.market.constants.MavenConstants.APP_ZIP_POSTFIX;
import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;

@AllArgsConstructor
@RestController
@RequestMapping(PRODUCT_DETAILS)
@Tag(name = "Product Detail Controllers", description = "API collection to get product's detail.")
public class ProductDetailsController {
  private final VersionService versionService;
  private final ProductService productService;
  private final ProductContentService productContentService;
  private final ProductDetailModelAssembler detailModelAssembler;
  private final GithubReleaseModelAssembler githubReleaseModelAssembler;
  private final PagedResourcesAssembler<GithubReleaseModel> pagedResourcesAssembler;

  @GetMapping(BY_ID_AND_VERSION)
  @Operation(summary = "Find product detail by product id and release version.",
      description = "get product detail by it product id and release version")
  public ResponseEntity<ProductDetailModel> findProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Release version (from maven metadata.xml)", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchProductDetailByIdAndVersion(id, version);
    if (productDetail == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version, BY_ID_AND_VERSION), HttpStatus.OK);
  }

  @GetMapping(BEST_MATCH_BY_ID_AND_VERSION)
  @Operation(summary = "Find best match product detail by product id and version.",
      description = "get product detail by it product id and version")
  public ResponseEntity<ProductDetailModel> findBestMatchProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Version", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchBestMatchProductDetail(id, version);
    if (productDetail == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version, BEST_MATCH_BY_ID_AND_VERSION),
        HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  @Operation(summary = "get product detail by ID", description = "Return product detail by product id (from meta.json)")
  public ResponseEntity<ProductDetailModel> findProductDetails(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @RequestParam(defaultValue = "false", name = SHOW_DEV_VERSION, required = false) @Parameter(description =
          "Option to get Dev Version (Snapshot/ sprint release)", in = ParameterIn.QUERY) Boolean isShowDevVersion) {
    var productDetail = productService.fetchProductDetail(id, isShowDevVersion);
    if (productDetail == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, BY_ID), HttpStatus.OK);
  }

  @GetMapping(VERSIONS_BY_ID)
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "adobe-acrobat-connector",
          in = ParameterIn.PATH) String id,
      @RequestParam(SHOW_DEV_VERSION) @Parameter(description = "Option to get Dev Version (Snapshot/ sprint release)",
          in = ParameterIn.QUERY) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY,
          example = "v10.0.20") String designerVersion) {
    List<MavenArtifactVersionModel> models =
        versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }

  @GetMapping(PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION)
  @Operation(summary = "Get product json content for designer to install",
      description = "When we click install in designer, this API will send content of product json for installing in " +
          "Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(ID) String productId,
      @PathVariable(VERSION) String version) {
    Map<String, Object> productJsonContent = versionService.getProductJsonContentByIdAndVersion(productId, version);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }

  @GetMapping(VERSIONS_IN_DESIGNER)
  @Operation(summary = "Get the list of released version in product",
      description = "Collect the released versions in product for ivy designer")
  public ResponseEntity<List<VersionAndUrlModel>> findVersionsForDesigner(@PathVariable(ID) String id) {
    List<VersionAndUrlModel> versionList = versionService.getVersionsForDesigner(id);
    return new ResponseEntity<>(versionList, HttpStatus.OK);
  }

  @GetMapping(LATEST_ARTIFACT_DOWNLOAD_URL_BY_ID)
  @Operation(summary = "Get the download url of latest version from artifact by its id and target version",
      description = "Return the download url of artifact from version and id")
  public ResponseEntity<String> getLatestArtifactDownloadUrl(
      @PathVariable(value = ID) @Parameter(in = ParameterIn.PATH, example = "demos-app") String productId,
      @RequestParam(value = VERSION) @Parameter(in = ParameterIn.QUERY, example = "10.0-dev") String version,
      @RequestParam(value = ARTIFACT) @Parameter(in = ParameterIn.QUERY,
          example = "ivy-demos-app.zip") String artifactId) {
    String downloadUrl = versionService.getLatestVersionArtifactDownloadUrl(productId, version, artifactId);
    HttpStatusCode statusCode = StringUtils.isBlank(downloadUrl) ? HttpStatus.NOT_FOUND : HttpStatus.OK;
    return new ResponseEntity<>(downloadUrl, statusCode);
  }

  @GetMapping(PRODUCT_PUBLIC_RELEASES)
  @Operation(summary = "Find public releases by product id",
      description = "Get all public releases by product id", parameters = {
      @Parameter(name = "page", description = "Page number to retrieve", in = ParameterIn.QUERY, example = "0",
          required = true),
      @Parameter(name = "size", description = "Number of items per page", in = ParameterIn.QUERY, example = "20",
          required = true)})
  public ResponseEntity<PagedModel<GithubReleaseModel>> findGithubPublicReleases(
      @PathVariable(ID) @Parameter(description = "Product id", example = "portal",
          in = ParameterIn.PATH) String productId,
      @ParameterObject Pageable pageable) throws IOException {
    Page<GithubReleaseModel> results = productService.getGitHubReleaseModels(productId, pageable);

    if (results.isEmpty()) {
      return generateReleasesEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, githubReleaseModelAssembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @GetMapping(SYNC_RELEASE_NOTES_FOR_PRODUCTS)
  public void syncLatestReleasesForProducts() throws IOException {
    Pageable pageable = PageRequest.of(0, 20, Sort.unsorted());
    List<String> productIdList = this.productService.getProductIdList();
    for (String productId : productIdList) {
      this.productService.getGitHubReleaseModels(productId, pageable);
    }
  }

  @GetMapping(PRODUCT_PUBLIC_RELEASE_BY_RELEASE_ID)
  @Operation(summary = "Find release by product id and release id",
      description = "Get release by product id and release id")
  public ResponseEntity<GithubReleaseModel> findGithubPublicReleaseByProductIdAndReleaseId(
      @PathVariable(PRODUCT_ID) @Parameter(description = "Product id", example = "portal",
          in = ParameterIn.PATH) String productId,
      @PathVariable(RELEASE_ID) @Parameter(description = "Release id", example = "67a08dd6e23661019dc92376",
          in = ParameterIn.PATH) Long releaseId) throws IOException {
    GithubReleaseModel githubReleaseModel = productService.getGitHubReleaseModelByProductIdAndReleaseId(productId, releaseId);
    return ResponseEntity.ok(githubReleaseModelAssembler.toModel(githubReleaseModel));
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<GithubReleaseModel>> generateReleasesEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<GithubReleaseModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        GithubReleaseModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }

  @GetMapping(ARTIFACTS_AS_ZIP)
  @Operation(summary = "Get the download steam of artifact and it's dependencies by it's id and target version",
      description = "Return the download url of artifact from version and id")
  public CompletableFuture<ResponseEntity<ResponseBodyEmitter>> downloadZipArtifact(
      @PathVariable(value = ID) @Parameter(in = ParameterIn.PATH, example = "demos") String id,
      @RequestParam(value = VERSION) @Parameter(in = ParameterIn.QUERY, example = "10.0") String version,
      @RequestParam(value = ARTIFACT) @Parameter(in = ParameterIn.QUERY, example = "demos-app") String artifactId) {
    // Open stream as async to save the server resources
    return productContentService.downloadZipArtifactFile(id, artifactId, version)
        .thenApply(emitter -> {
          if (emitter == null) {
            var noDataBody = new ResponseBodyEmitter();
            noDataBody.complete();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(noDataBody);
          }

          // Prepare the response
          String filename = artifactId.concat(APP_ZIP_POSTFIX);
          HttpHeaders headers = new HttpHeaders();
          headers.add(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_HEADER.formatted(filename));
          headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

          return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(emitter);
        });
  }

  @GetMapping("/get-cache/{product-id}")
  public void getProductCache(
      @PathVariable(PRODUCT_ID) @Parameter(description = "Product id", example = "portal",
          in = ParameterIn.PATH) String productId) throws IOException {
    productService.getProductCache(productId);
  }
}
