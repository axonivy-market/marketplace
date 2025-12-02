package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.ExternalDocumentMeta;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@RequestMapping(EXTERNAL_DOCUMENT)
@Tag(name = "External document Controller", description = "API collection to get and search for the external document")
@AllArgsConstructor
public class ExternalDocumentController {
  private final ExternalDocumentService externalDocumentService;
  private final GitHubService gitHubService;

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

    var model = ExternalDocumentModel.from(externalDocument);
    model.add(linkTo(methodOn(ExternalDocumentController.class).findExternalDocument(id, version)).withSelfRel());
    return new ResponseEntity<>(model, HttpStatus.OK);
  }

  @GetMapping(DOCUMENT_BEST_MATCH)
  public ResponseEntity<Void> redirectToBestVersion(@RequestParam(value = "path", required = false) String path) {
    ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.FOUND);
    String responseURL = ERROR_PAGE_404;
    String redirectUrl = externalDocumentService.resolveBestMatchRedirectUrl(path);
    if (redirectUrl != null) {
      responseURL = redirectUrl;
    }
    return response.location(URI.create(responseURL)).build();
  }

  @PutMapping(SYNC)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncDocumentForProduct(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false, defaultValue = "false") Boolean resetSync,
      @RequestParam(value = PRODUCT_ID, required = false) String productId,
      @RequestParam(value = VERSION, required = false) String version) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token,
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    List<String> productIds = externalDocumentService.determineProductIdsForSync(productId);

    if (ObjectUtils.isEmpty(productIds)) {
      var message = new Message(ErrorCode.NOTHING_TO_SYNC.getCode(), ErrorCode.NOTHING_TO_SYNC.getHelpText(), null);
      return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }

    for (String id : productIds) {
      externalDocumentService.syncDocumentForProduct(id, resetSync, version);
    }

    var message = new Message(ErrorCode.SUCCESSFUL.getCode(), ErrorCode.SUCCESSFUL.getHelpText(), null);
    return new ResponseEntity<>(message, HttpStatus.OK);
  }

}
