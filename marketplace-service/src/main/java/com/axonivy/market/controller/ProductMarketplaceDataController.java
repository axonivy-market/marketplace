package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.logging.Loggable;
import com.axonivy.market.model.Message;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import static com.axonivy.market.constants.MavenConstants.APP_ZIP_FORMAT;
import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT_MARKETPLACE_DATA)
@Tag(name = "Product Marketplace Data Controller", description = "API collection to get product marketplace data")
@AllArgsConstructor
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
  @GetMapping(value = "/zip-file/{id}", produces = "application/octet-stream")
  public ResponseEntity<Object> extractUrlFile(
      @PathVariable(ID) String productId,
      @RequestParam(name = "url") String artifactUrl) throws IOException {
    ByteArrayResource resource = productMarketplaceDataService.downloadArtifact(artifactUrl, productId);

    if (resource == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).body("File not found or download failed.");
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDisposition(ContentDisposition.attachment()
        .filename(getFileNameFromUrl(artifactUrl))
        .build());

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(resource.contentLength())
        .body(resource);
  }

  private String getFileNameFromUrl(String artifactUrl) {
    return artifactUrl.substring(artifactUrl.lastIndexOf("/") + 1);
  }

}
