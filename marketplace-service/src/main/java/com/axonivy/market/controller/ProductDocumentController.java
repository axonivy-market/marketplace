package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Message;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DOC;
import static com.axonivy.market.constants.RequestMappingConstants.SYNC;
import static com.axonivy.market.constants.RequestParamConstants.RESET_SYNC;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(PRODUCT_DOC)
@Tag(name = "Product Controller", description = "API collection to get and search products")
@AllArgsConstructor
public class ProductDocumentController {
  private final ProductService productService;
  private final GitHubService gitHubService;

  @PutMapping(SYNC)
  @Operation(hidden = true)
  public ResponseEntity<Message> syncDocumentForProduct(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @RequestParam(value = RESET_SYNC, required = false) Boolean resetSync) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserOrganization(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);

    var message = new Message();
    message.setHelpCode(ErrorCode.SUCCESSFUL.getCode());
    message.setHelpText(ErrorCode.SUCCESSFUL.getHelpText());
    return new ResponseEntity<>(message, HttpStatus.OK);
  }
}
