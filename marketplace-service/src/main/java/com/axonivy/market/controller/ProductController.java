package com.axonivy.market.controller;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Message;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT)
@AllArgsConstructor
@Tag(name = "Product Controller", description = "API collection to get and search products")
public class ProductController {
  private final ProductService productService;
  private final GitHubService gitHubService;
  private final ProductModelAssembler assembler;
  private final PagedResourcesAssembler<Product> pagedResourcesAssembler;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final ProductDependencyService productDependencyService;

  @GetMapping()
  @Operation(summary = "Retrieve a paginated list of all products, optionally filtered by type, keyword, and language",
      description = "By default, the system finds products with type 'all'", parameters = {
      @Parameter(name = "page", description = "Page number to retrieve", in = ParameterIn.QUERY, example = "0",
          required = true),
      @Parameter(name = "size", description = "Number of items per page", in = ParameterIn.QUERY, example = "20",
          required = true),
      @Parameter(name = "sort",
          description = "Sorting criteria in the format: Sorting criteria(popularity|alphabetically|recent), Sorting " +
              "order(asc|desc)",
          in = ParameterIn.QUERY, example = "[\"popularity\",\"asc\"]", required = true)})
  public ResponseEntity<PagedModel<ProductModel>> findProducts(
      @RequestParam(name = TYPE) @Parameter(description = "Type of product.", in = ParameterIn.QUERY,
          schema = @Schema(type = "string",
              allowableValues = {"all", "connectors", "utilities", "solutions", "demos"})) String type,
      @RequestParam(required = false, name = KEYWORD) @Parameter(
          description = "Keyword that exist in product's name or short description", example = "connector",
          in = ParameterIn.QUERY) String keyword,
      @RequestParam(name = LANGUAGE) @Parameter(description = "Language of product short description",
          in = ParameterIn.QUERY, schema = @Schema(allowableValues = {"en", "de"})) String language,
      @RequestParam(name = IS_REST_CLIENT) @Parameter(
          description = "Option to render the website in the REST Client Editor of Designer",
          in = ParameterIn.QUERY) Boolean isRESTClient,
      @ParameterObject Pageable pageable) {
    Page<Product> results = productService.findProducts(type, keyword, language, isRESTClient, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, assembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @PutMapping(SYNC)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncProducts(@RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false) Boolean resetSync) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    var stopWatch = new StopWatch();
    stopWatch.start();
    List<String> syncedProductIds = productService.syncLatestDataFromMarketRepo(resetSync);
    var message = new Message();
    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    if (ObjectUtils.isEmpty(syncedProductIds)) {
      message.setMessageDetails("Data is already up to date, nothing to sync");
    } else {
      stopWatch.stop();
      message.setMessageDetails(String.format("Finished sync [%s] data in [%s] milliseconds", syncedProductIds,
          stopWatch.getTime()));
    }
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  @PutMapping(SYNC_ONE_PRODUCT_BY_ID)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncOneProduct(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @PathVariable(ID) @Parameter(description = "Product Id is defined in meta.json file", example = "a-trust",
          in = ParameterIn.PATH) String productId,
      @RequestParam(value = MARKET_ITEM_PATH) @Parameter(
          description = "Item folder path of the market in https://github.com/axonivy-market/market",
          example = "market/connector/a-trust") String marketItemPath,
      @RequestParam(value = OVERRIDE_MARKET_ITEM_PATH, required = false) Boolean overrideMarketItemPath) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    var message = new Message();
    if (StringUtils.isNotBlank(marketItemPath) && Boolean.TRUE.equals(
        overrideMarketItemPath) && CollectionUtils.isEmpty(
        axonIvyMarketRepoService.getMarketItemByPath(marketItemPath))) {
      message.setHelpCode(ErrorCode.PRODUCT_NOT_FOUND.getCode());
      message.setMessageDetails(ErrorCode.PRODUCT_NOT_FOUND.getHelpText());
      return new ResponseEntity<>(message, HttpStatus.OK);
    }

    var isSuccess = productService.syncOneProduct(productId, marketItemPath, overrideMarketItemPath);
    if (isSuccess) {
      message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
      message.setMessageDetails("Sync successfully!");
    } else {
      message.setMessageDetails("Sync unsuccessfully!");
    }
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  @PutMapping(SYNC_FIRST_PUBLISHED_DATE_ALL_PRODUCTS)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncFirstPublishedDateOfAllProducts(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    var message = new Message();
    var isSuccess = productService.syncFirstPublishedDateOfAllProducts();
    if (isSuccess) {
      message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
      message.setMessageDetails("Sync successfully!");
    } else {
      message.setMessageDetails("Sync unsuccessfully!");
    }
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<ProductModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<ProductModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        ProductModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }

  @Operation(hidden = true)
  @PutMapping(SYNC_ZIP_ARTIFACTS)
  public ResponseEntity<Message> syncProductArtifacts(@RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false) Boolean resetSync,
      @RequestParam(value = ID, required = false) String productId) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    var message = new Message();
    int syncIds = productDependencyService.syncIARDependenciesForProducts(resetSync, productId);
    if (syncIds > 0) {
      message.setMessageDetails("Synced %d artifact(s)".formatted(syncIds));
      return ResponseEntity.status(HttpStatus.OK).body(message);
    }
    message.setMessageDetails("Nothing to sync");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(message);
  }
}
