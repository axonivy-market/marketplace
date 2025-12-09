package com.axonivy.market.controller;

import com.axonivy.market.logging.Loggable;
import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.service.ProductDesignerInstallationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.DESIGNER_INSTALLATION_BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DESIGNER_INSTALLATION;
import static com.axonivy.market.constants.RequestParamConstants.ID;

@RestController
@RequestMapping(PRODUCT_DESIGNER_INSTALLATION)
@AllArgsConstructor
@Tag(name = "Product Designer Installation Controllers",
    description = "API collection to get designer installation count.")
public class ProductDesignerInstallationController {
  private final ProductDesignerInstallationService productDesignerInstallationService;

  @Loggable
  @GetMapping(DESIGNER_INSTALLATION_BY_ID)
  @Operation(summary = "Get designer installation count by product id.",
      description = "get designer installation count by product id")
  public ResponseEntity<List<DesignerInstallation>> getProductDesignerInstallationByProductId(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "adobe-acrobat-connector",
          in = ParameterIn.PATH) String productId) {
    List<DesignerInstallation> models = productDesignerInstallationService.findByProductId(productId);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }
}
