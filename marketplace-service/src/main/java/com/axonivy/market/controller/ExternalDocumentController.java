package com.axonivy.market.controller;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.ExternalDocumentModel;
import com.axonivy.market.model.Message;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.DirectoryConstants.CACHE_DIR;
import static com.axonivy.market.constants.DirectoryConstants.DATA_CACHE_DIR;
import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static com.axonivy.market.util.DocPathUtils.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Log4j2
@RestController
@RequestMapping(EXTERNAL_DOCUMENT)
@Tag(name = "External document Controller", description = "API collection to get and search for the external document")
@AllArgsConstructor
public class ExternalDocumentController {
  final ExternalDocumentService externalDocumentService;
  final GitHubService gitHubService;

  private static final String UPDATE_PATH_FORMAT = "/%s/%s/%s%s";

  @GetMapping(BY_ID_AND_VERSION)
  public ResponseEntity<ExternalDocumentModel> findExternalDocument(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "portal",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Release version (from maven metadata.xml)", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    ExternalDocumentMeta externalDocument = externalDocumentService.findExternalDocument(id, version);
    if (externalDocument == null) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    var model = ExternalDocumentModel.builder().productId(externalDocument.getProductId())
        .version(externalDocument.getVersion()).relativeLink(externalDocument.getRelativeLink())
        .artifactName(externalDocument.getArtifactName()).build();
    model.add(linkTo(methodOn(ExternalDocumentController.class).findExternalDocument(id, version)).withSelfRel());

    return new ResponseEntity<>(model, HttpStatus.OK);
  }

  @GetMapping(DOCUMENT_BEST_MATCH)
  public ResponseEntity<Void> redirectToBestVersion(@RequestParam(value = "path", required = false) String path) {
       ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.FOUND);
    String version = extractVersion(path);
    String productId = extractProductId(path);
    log.info("Request to redirect to best match version for productId: {}, version: {}, path: {}",
        productId, version, path);
    if (productId != null && version != null && path != null) {
      String bestMatchVersion = externalDocumentService.findBestMatchVersion(productId, version);
      // Replace the old version with the best matched version
      String updatedPath = updateVersionInPath(path, bestMatchVersion, version);
      log.info("Best match version: {}, updatedPath: {}", bestMatchVersion, updatedPath);
      Path baseDir = Paths.get(DATA_CACHE_DIR).toAbsolutePath().normalize();
      Path relativePath = Paths.get(updatedPath).normalize();
      if (relativePath.isAbsolute()) {
        relativePath = Paths.get(updatedPath.substring(1)).normalize();
      }
      Path resolvedPath = baseDir.resolve(relativePath).normalize();
      if (!resolvedPath.startsWith(baseDir)) {
        log.warn("Path traversal attempt detected: {}", updatedPath);
        return response.location(URI.create(ERROR_PAGE_404)).build();
      }

      if (!Files.exists(resolvedPath)) {
        log.warn("File not found: {}", resolvedPath);
        return response.location(URI.create(ERROR_PAGE_404)).build();
      }

      return response.location(URI.create(SLASH + CACHE_DIR + updatedPath)).build();
    }
    return response.location(URI.create(ERROR_PAGE_404)).build();
  }

  @PutMapping(SYNC)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncDocumentForProduct(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false, defaultValue = "false") Boolean resetSync,
      @RequestParam(value = PRODUCT_ID, required = false) String productId,
      @RequestParam(value = VERSION, required = false) String version) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token,
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    List<String> productIds;
    if (ObjectUtils.isNotEmpty(productId)) {
      productIds = List.of(productId);
    } else {
      productIds = externalDocumentService.findAllProductsHaveDocument().stream()
          .map(Product::getId).toList();
    }
    var message = new Message();
    if (ObjectUtils.isEmpty(productIds)) {
      message.setHelpText("Nothing to be synced");
      return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }

    for (String id : productIds) {
      externalDocumentService.syncDocumentForProduct(id, resetSync, version);
    }

    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    return new ResponseEntity<>(message, HttpStatus.OK);
  }
}
