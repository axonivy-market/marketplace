package com.axonivy.market.service;

import com.axonivy.market.model.AdminGitHubCallbackRequest;
import com.axonivy.market.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AdminSessionAuthService {
  String createAuthorizationState(HttpServletRequest request);

  UserInfo authenticate(AdminGitHubCallbackRequest callbackRequest, HttpServletRequest request,
      HttpServletResponse response);
}
