package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.SECURITY_MONITOR;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(SECURITY_MONITOR)
@Tag(name = "Security Monitor Controllers", description = "API collection to get Github Marketplace security's detail.")
@AllArgsConstructor
public class SecurityMonitorController {
  private final GitHubService gitHubService;

  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<Object> getGitHubMarketplaceSecurity(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    List<ProductSecurityInfo> securityInfoList = gitHubService.getSecurityDetailsForAllProducts(token,
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    return ResponseEntity.ok(securityInfoList);
  }
}
