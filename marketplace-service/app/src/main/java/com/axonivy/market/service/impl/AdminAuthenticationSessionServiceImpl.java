package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.repository.PasskeyCredentialRepository;
import com.axonivy.market.service.AdminAuthenticationSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthenticationSessionServiceImpl implements AdminAuthenticationSessionService {
  private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
  private final SecurityContextRepository securityContextRepository;
  private final PasskeyCredentialRepository passkeyCredentialRepository;

  @Override
  public UserInfo createSession(GithubUser githubUser, String profileUrl, HttpServletRequest request,
      HttpServletResponse response) {
    UserInfo sessionUser = toSessionUser(githubUser, profileUrl);
    var authentication = UsernamePasswordAuthenticationToken.authenticated(sessionUser, null,
        AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

    sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    securityContextRepository.saveContext(securityContext, request, response);
    return sessionUser;
  }

  private UserInfo toSessionUser(GithubUser githubUser, String profileUrl) {
    UserInfo sessionUser = new UserInfo();
    sessionUser.setId(githubUser.getId());
    sessionUser.setGitHubId(githubUser.getGitHubId());
    sessionUser.setProvider(githubUser.getProvider());
    sessionUser.setUsername(githubUser.getUsername());
    sessionUser.setName(githubUser.getName());
    sessionUser.setAvatarUrl(githubUser.getAvatarUrl());
    sessionUser.setUrl(resolveProfileUrl(githubUser, profileUrl));
    sessionUser.setToken(null);
    sessionUser.setHasPasskey(passkeyCredentialRepository.findByGithubUserId(githubUser.getId()).isPresent());
    return sessionUser;
  }

  private String resolveProfileUrl(GithubUser githubUser, String profileUrl) {
    if (StringUtils.isNotBlank(profileUrl)) {
      return profileUrl;
    }
    if (StringUtils.isBlank(githubUser.getUsername())) {
      return null;
    }
    return "https://github.com/" + githubUser.getUsername();
  }
}
