package com.axonivy.market.controller;

import com.axonivy.market.bo.VersionDownload;
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
import org.springframework.http.HttpStatus;
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

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.URL;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT_MARKETPLACE_DATA)
@Tag(name = "Product Marketplace Data Controller", description = "API collection to get product marketplace data")
@AllArgsConstructor
@Validated
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
  public ResponseEntity<VersionDownload> extractArtifactUrl(
      @PathVariable(ID) String productId,
      @RequestParam(URL) @ValidUrl String artifactUrl) {

    VersionDownload result = productMarketplaceDataService.downloadArtifact(artifactUrl, productId);

    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping(INSTALLATION_COUNT_BY_ID)
  public ResponseEntity<Integer> findInstallationCount(@PathVariable(ID)
  String id) {
    Integer result = productMarketplaceDataService.getInstallationCount(id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}