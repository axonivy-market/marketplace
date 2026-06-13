package com.axonivy.market.service;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AdminAuthenticationSessionService {
  UserInfo createSession(GithubUser githubUser, String profileUrl, HttpServletRequest request, HttpServletResponse response);
}
