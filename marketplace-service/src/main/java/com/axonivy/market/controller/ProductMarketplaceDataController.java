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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;

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

//  @Operation(hidden = true)
//  @GetMapping(VERSION_DOWNLOAD_BY_ID)
//  public ResponseEntity<VersionDownload> extractArtifactUrl(
//      @PathVariable(ID) String productId,
//      @RequestParam(URL) @ValidUrl String artifactUrl) {
//
//    VersionDownload result = productMarketplaceDataService.downloadArtifact(artifactUrl, productId);
//
//    if (result == null) {
//      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }
//    return new ResponseEntity<>(result, HttpStatus.OK);
//  }

  @Operation(hidden = true)
  @GetMapping(VERSION_DOWNLOAD_BY_ID)
  public void extractArtifactUrl(
      @PathVariable(ID) String productId,
      @RequestParam(URL) @ValidUrl String artifactUrl, HttpServletResponse response) {
    RestTemplate restTemplate = new RestTemplateBuilder().build();
    try {
      ResponseEntity<Resource> resourceResponse = restTemplate.exchange(
          artifactUrl,
          HttpMethod.GET,
          null,
          Resource.class
      );

      if (!resourceResponse.getStatusCode().is2xxSuccessful() || resourceResponse.getBody() == null) {
        log.warn("Failed to retrieve file from URL: {}. Status: {}", artifactUrl, resourceResponse.getStatusCode());
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        return;
      }

      Resource resource = resourceResponse.getBody();

      // Determine filename
      String filename = productId + ".zip"; // fallback default
      String contentDispositionHeader = resourceResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
      if (contentDispositionHeader != null) {
        // If header includes filename, extract it
        filename = contentDispositionHeader.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
      }

      // Set headers
      response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

      // Stream from remote URL to user
      try (InputStream in = resource.getInputStream();
           OutputStream out = response.getOutputStream()) {

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
        out.flush();

        // ✅ Increment only after full delivery
        int count = productMarketplaceDataService.updateInstallationCountForProduct(productId, null);
        response.setHeader("X-Installation-Count", String.valueOf(count));

        log.info("File {} successfully downloaded and installation count incremented.", productId);

      } catch (Exception e) {
        log.warn("Download stream failed or canceled for file {}: {}", productId, e.getMessage());
        // Do not increment
      }

    } catch (Exception e) {
      log.error("Error processing file download for file {}: {}", productId, e.getMessage(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(INSTALLATION_COUNT_BY_ID)
  public ResponseEntity<Integer> findInstallationCount(@PathVariable(ID)
  String id) {
    Integer result = productMarketplaceDataService.getInstallationCount(id);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}