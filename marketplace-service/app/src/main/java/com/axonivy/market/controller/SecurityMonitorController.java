package com.axonivy.market.controller;

import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.SECURITY_MONITOR;

@RestController
@RequestMapping(SECURITY_MONITOR)
@Tag(name = "Security Monitor Controllers", description = "API collection to get Github Marketplace security's detail.")
@AllArgsConstructor
public class SecurityMonitorController {
  private final GitHubService gitHubService;

  @Authorized
  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<Object> getGitHubMarketplaceSecurity(HttpServletRequest request) throws IOException {
    String token = (String) request.getAttribute(AuthorizedAspect.VALIDATED_TOKEN_ATTRIBUTE);
    List<ProductSecurityInfo> securityInfoList = gitHubService.getSecurityDetailsForAllProducts(token,
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    return ResponseEntity.ok(securityInfoList);
  }
}
