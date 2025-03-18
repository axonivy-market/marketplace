package com.axonivy.market.controller;

import com.axonivy.market.bo.VersionDownload;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.logging.Loggable;
import com.axonivy.market.model.Message;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.DESIGNER_VERSION;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT_MARKETPLACE_DATA)
@Tag(name = "Product Marketplace Data Controller", description = "API collection to get product marketplace data")
@AllArgsConstructor
public class ProductMarketplaceDataController {
  private final GitHubService gitHubService;
  private final ProductMarketplaceDataService productMarketplaceDataService;
  private final ProductMarketplaceDataRepository productMarketplaceDataRepository;

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

  @Loggable
  @Operation(hidden = true)
  @PutMapping(INSTALLATION_COUNT_BY_ID)
  public ResponseEntity<Integer> syncInstallationCount(
      @PathVariable(ID) String productId,
      @RequestParam(name = DESIGNER_VERSION, required = false) String designerVersion) {
    int result = productMarketplaceDataService.updateInstallationCountForProduct(productId, designerVersion);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @Operation(hidden = true)
  @GetMapping(VERSION_DOWNLOAD_BY_ID)
  public ResponseEntity<VersionDownload> extractArtifactUrl(
      @PathVariable(ID) String productId,
      @RequestParam(name = "url") String artifactUrl) throws IOException {
    VersionDownload result = productMarketplaceDataService.downloadArtifact(artifactUrl, productId);

    if (result == null) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("hello/{id}")
  public ResponseEntity<Integer> findInstallationCount(@PathVariable(ID)
  String id) {
    ProductMarketplaceData data = productMarketplaceDataRepository.findById(id).orElse(null);
    if (data == null) {
      return new ResponseEntity<>(0, HttpStatus.OK);
    }
    return new ResponseEntity<>(data.getInstallationCount(), HttpStatus.OK);
  }
}
