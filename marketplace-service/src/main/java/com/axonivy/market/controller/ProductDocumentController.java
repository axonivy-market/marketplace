package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Message;
import com.axonivy.market.service.ProductDocumentService;
import com.axonivy.market.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT_DOC)
@Tag(name = "Product Controller", description = "API collection to get and search products")
@AllArgsConstructor
public class ProductDocumentController {
  final ProductDocumentService productDocumentService;
  final GitHubService gitHubService;

  @GetMapping(BY_ID_AND_VERSION)
  public ResponseEntity<URI> findViewDocURI(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Release version (from maven metadata.xml)", example = "10.0.20",
          in = ParameterIn.PATH) String version) throws URISyntaxException {
    String viewDocURI = productDocumentService.findViewDocURI(id, version);
    if (StringUtils.isBlank(viewDocURI)) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    var uri = new URI(viewDocURI);
    return new ResponseEntity<>(uri, HttpStatus.OK);
  }

  @PutMapping(SYNC)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncDocumentForProduct(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false) Boolean resetSync) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserOrganization(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    var message = new Message();
    List<Product> products = productDocumentService.findAllProductsHaveDocument();
    if (ObjectUtils.isEmpty(products)) {
      return new ResponseEntity<>(message, HttpStatus.NO_CONTENT);
    }
    products.forEach(product -> {
      productDocumentService.syncDocumentForProduct(product.getId(), resetSync);
    });

    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    return new ResponseEntity<>(message, HttpStatus.OK);
  }
}
