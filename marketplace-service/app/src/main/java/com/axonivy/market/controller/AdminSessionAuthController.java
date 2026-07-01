package com.axonivy.market.controller;

import com.axonivy.market.model.AdminGitHubAuthorizationState;
import com.axonivy.market.model.AdminGitHubCallbackRequest;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.service.AdminSessionAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static com.axonivy.market.constants.RequestMappingConstants.ADMIN_AUTH_V2;
import static com.axonivy.market.constants.RequestMappingConstants.CSRF;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_AUTHORIZATION;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_CALLBACK;
import static com.axonivy.market.constants.RequestMappingConstants.SESSION;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping(ADMIN_AUTH_V2)
public class AdminSessionAuthController {
  private final AdminSessionAuthService adminSessionAuthService;

  @GetMapping(GITHUB_AUTHORIZATION)
  public AdminGitHubAuthorizationState createAuthorizationState(HttpServletRequest request) {
    return new AdminGitHubAuthorizationState(adminSessionAuthService.createAuthorizationState(request));
  }

  @GetMapping(CSRF)
  public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
    return ResponseEntity.noContent().build();
  }

  @GetMapping(SESSION)
  public UserInfo session(@AuthenticationPrincipal UserInfo currentUser) {
    if (currentUser == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin session not found");
    }
    return currentUser;
  }

  @PostMapping(GITHUB_CALLBACK)
  public UserInfo exchangeCode(@RequestBody AdminGitHubCallbackRequest callbackRequest, HttpServletRequest request,
      HttpServletResponse response) {
    return adminSessionAuthService.authenticate(callbackRequest, request, response);
  }
}
