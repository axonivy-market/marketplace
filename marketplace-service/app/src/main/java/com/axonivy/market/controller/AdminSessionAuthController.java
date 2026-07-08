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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.constants.RequestMappingConstants.ADMIN_AUTH;
import static com.axonivy.market.constants.RequestMappingConstants.CSRF;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_AUTHORIZATION;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_CALLBACK;
import static com.axonivy.market.constants.RequestMappingConstants.SESSION;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping(ADMIN_AUTH)
public class AdminSessionAuthController {
  private final AdminSessionAuthService adminSessionAuthService;

  @GetMapping(GITHUB_AUTHORIZATION)
  public AdminGitHubAuthorizationState createAuthorizationState(HttpServletRequest request) {
    return new AdminGitHubAuthorizationState(adminSessionAuthService.createAuthorizationState(request));
  }

  @GetMapping(CSRF)
  public ResponseEntity<Void> csrf() {
    return ResponseEntity.noContent().build();
  }

  @GetMapping(SESSION)
  public ResponseEntity<UserInfo> session(@AuthenticationPrincipal UserInfo currentUser) {
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(currentUser);
  }

  @PostMapping(GITHUB_CALLBACK)
  public ResponseEntity<UserInfo> exchangeCode(@RequestBody AdminGitHubCallbackRequest callbackRequest, HttpServletRequest request,
      HttpServletResponse response) {
    return  ResponseEntity.ok(adminSessionAuthService.authenticate(callbackRequest, request, response));
  }
}
