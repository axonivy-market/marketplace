package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Message;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import com.axonivy.market.util.validator.ValidUrl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Optional;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.URL;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT_MARKETPLACE_DATA)
@Tag(name = "Product Marketplace Data Controller", description = "API collection to get product marketplace data")
@AllArgsConstructor
@Validated
@Log4j2
public class ProductMarketplaceDataController {
  private final GitHubService gitHubService;
  private final ProductMarketplaceDataService productMarketplaceDataService;

  @PostMapping(CUSTOM_SORT)
  @Operation(hidden = true)
  public ResponseEntity<Message> createCustomSortProducts(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestBody @Valid ProductCustomSortRequest productCustomSortRequest) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    productMarketplaceDataService.addCustomSortProduct(productCustomSortRequest);
    var message = new Message(ErrorCode.SUCCESSFUL.getCode(), ErrorCode.SUCCESSFUL.getHelpText(),
        "Custom product sort order added successfully");
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

  @Operation(hidden = true)
  @GetMapping(VERSION_DOWNLOAD_BY_ID)
  public ResponseEntity<StreamingResponseBody> extractArtifactUrl(@PathVariable(ID) String productId,
      @RequestParam(URL) @ValidUrl String artifactUrl) {
    ResponseEntity<Resource> resourceResponse = productMarketplaceDataService.fetchResourceUrl(artifactUrl);
    HttpStatusCode statusCode = Optional.ofNullable(resourceResponse).map(ResponseEntity::getStatusCode).orElse(
        HttpStatus.NOT_FOUND);
    Resource body = Optional.ofNullable(resourceResponse).map(ResponseEntity::getBody).orElse(null);
    if (null == body || !statusCode.is2xxSuccessful()) {
      log.warn("Failed to retrieve file from URL: {}. Status: {}", artifactUrl, statusCode);
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
    StreamingResponseBody streamingBody = outputStream -> productMarketplaceDataService.buildArtifactStreamFromResource(
        productId, body, outputStream);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment").body(streamingBody);
  }

  @GetMapping(INSTALLATION_COUNT_BY_ID)
  public ResponseEntity<Integer> findInstallationCount(@PathVariable(ID)
  String id) {
    Integer result = productMarketplaceDataService.getInstallationCount(id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}