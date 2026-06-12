package com.axonivy.market.controller;

import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.ProductSecuritySortOption;
import com.axonivy.market.github.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.axonivy.market.constants.PostgresDBConstants.ASCENDING;
import static com.axonivy.market.constants.RequestMappingConstants.SECURITY_MONITOR;
import static com.axonivy.market.constants.RequestParamConstants.*;

@RestController
@RequestMapping(SECURITY_MONITOR)
@Tag(name = "Security Monitor Controllers", description = "API collection to get Github Marketplace security's detail.")
@AllArgsConstructor
public class SecurityMonitorController {

  private final GitHubService gitHubService;

  @PostMapping
  @Operation(hidden = true)
  public ResponseEntity<List<ProductSecurityInfo>> syncGitHubMarketplaceSecurity() throws IOException {
    List<ProductSecurityInfo> securityInfoList = gitHubService.syncSecurityDetailsForProduct();
    return ResponseEntity.ok(securityInfoList);
  }

  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<PagedModel<ProductSecurityInfo>> getGitHubMarketplaceSecurity(
      @RequestParam(value = SEARCH, required = false) String searchText,
      @RequestParam(value = SORT, required = false, defaultValue = "repoName") String sort,
      @RequestParam(value = SORT_DIRECTION, required = false, defaultValue = ASCENDING) String sortDirection,
      @ParameterObject Pageable pageable) throws IOException {
    ProductSecurityCriteria securityCriteria = buildCriteria(searchText, sort, sortDirection);
    Page<ProductSecurityInfo> results = gitHubService.searchSecurityDetails(securityCriteria, pageable);
    var pageMetadata = new PagedModel.PageMetadata(results.getSize(), results.getNumber(),
        results.getTotalElements(), results.getTotalPages());
    PagedModel<ProductSecurityInfo> pagedModel = PagedModel.of(results.getContent(), pageMetadata);
    return ResponseEntity.ok(pagedModel);
  }

  private ProductSecurityCriteria buildCriteria(String searchText, String sortOption,
      String sortDirection) {
    return ProductSecurityCriteria.builder()
        .searchText(searchText)
        .sortOption(ProductSecuritySortOption.of(sortOption))
        .sortDirection(sortDirection).build();
  }

}
