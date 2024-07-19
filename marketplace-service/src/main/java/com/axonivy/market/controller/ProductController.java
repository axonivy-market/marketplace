package com.axonivy.market.controller;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.Message;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT;
import static com.axonivy.market.constants.RequestMappingConstants.SYNC;

@RestController
@RequestMapping(PRODUCT)
public class ProductController {

  private final ProductService productService;
  private final GitHubService gitHubService;
  private final ProductModelAssembler assembler;
  private final PagedResourcesAssembler<Product> pagedResourcesAssembler;

  public ProductController(ProductService productService, GitHubService gitHubService, ProductModelAssembler assembler,
      PagedResourcesAssembler<Product> pagedResourcesAssembler) {
    this.productService = productService;
    this.gitHubService = gitHubService;
    this.assembler = assembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all products", description = "Be default system will finds product by type as 'all'")
  @GetMapping()
  public ResponseEntity<PagedModel<ProductModel>> findProducts(@RequestParam(name = "type") String type,
      @RequestParam(required = false, name = "keyword") String keyword,
      @RequestParam(name = "language") String language, Pageable pageable) {
    Page<Product> results = productService.findProducts(type, keyword, language, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, assembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @PutMapping(SYNC)
  public ResponseEntity<Message> syncProducts(@RequestHeader(value = "Authorization") String authorizationHeader) {
    String token = null;
    if (authorizationHeader.startsWith("Bearer ")) {
      token = authorizationHeader.substring(7); // Remove "Bearer " prefix
    }
    gitHubService.validateUserOrganization(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);

    var stopWatch = new StopWatch();
    stopWatch.start();
    var isAlreadyUpToDate = productService.syncLatestDataFromMarketRepo();
    var message = new Message();
    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    if (isAlreadyUpToDate) {
      message.setMessageDetails("Data is already up to date, nothing to sync");
    } else {
      stopWatch.stop();
      message.setMessageDetails(String.format("Finished sync data in [%s] milliseconds", stopWatch.getTime()));
    }
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<ProductModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<ProductModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        ProductModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }
}
