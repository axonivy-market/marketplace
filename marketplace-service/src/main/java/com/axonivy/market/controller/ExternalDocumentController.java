package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.ExternalDocumentModel;
import com.axonivy.market.model.Message;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(EXTERNAL_DOCUMENT)
@Tag(name = "External document Controller", description = "API collection to get and search for the external document")
@AllArgsConstructor
public class ExternalDocumentController {
  final ExternalDocumentService externalDocumentService;
  final GitHubService gitHubService;

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

  @PutMapping(SYNC)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncDocumentForProduct(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false) Boolean resetSync) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    var message = new Message();
    List<Product> products = externalDocumentService.findAllProductsHaveDocument();
    if (ObjectUtils.isEmpty(products)) {
      return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }

    for (Product product : products) {
      externalDocumentService.syncDocumentForProduct(product.getId(), new ArrayList<>(), resetSync);
    }

    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    return new ResponseEntity<>(message, HttpStatus.OK);
  }
}
