package com.axonivy.market.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.AdminGitHubCallbackRequest;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.service.AdminSessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminSessionAuthServiceImpl implements AdminSessionAuthService {
  private static final String OAUTH_STATE_SESSION_ATTRIBUTE = AdminSessionAuthServiceImpl.class.getName() + ".state";
  private static final Duration OAUTH_STATE_TTL = Duration.ofMinutes(5);

  private final GitHubProperty gitHubProperty;
  private final GitHubService gitHubService;
  private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
  private final SecurityContextRepository securityContextRepository;

  @Override
  public String createAuthorizationState(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    Map<String, Instant> pendingStates = getPendingStates(session);
    removeExpiredStates(pendingStates);

    String state = UUID.randomUUID().toString();
    pendingStates.put(state, Instant.now().plus(OAUTH_STATE_TTL));
    session.setAttribute(OAUTH_STATE_SESSION_ATTRIBUTE, pendingStates);

    return state;
  }

  @Override
  public UserInfo authenticate(AdminGitHubCallbackRequest callbackRequest, HttpServletRequest request,
      HttpServletResponse response) {
    validateCallbackRequest(callbackRequest);
    validateState(request.getSession(false), callbackRequest.getState());

    try {
      String accessToken = gitHubService.getAccessToken(callbackRequest.getCode(), gitHubProperty).getAccessToken();
      UserInfo authorizedGitHubUser = gitHubService.validateUserInOrganizationAndTeam(accessToken,
          GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME, GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
      GithubUser persistedUser = gitHubService.getAndUpdateUser(accessToken);
      UserInfo sessionUser = toSessionUser(authorizedGitHubUser, persistedUser);

      var authentication = UsernamePasswordAuthenticationToken.authenticated(sessionUser, null,
          AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

      sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(authentication);
      SecurityContextHolder.setContext(securityContext);
      securityContextRepository.saveContext(securityContext, request, response);

      log.info("Admin login success userId={} username={}", sessionUser.getId(), sessionUser.getUsername());
      return sessionUser;
    } catch (Oauth2ExchangeCodeException exception) {
      log.warn("Admin login failed during OAuth exchange: {}", exception.getErrorDescription());
      throw exception;
    } catch (Exception exception) {
      log.error("Admin login failed", exception);
      throw new Oauth2ExchangeCodeException(HttpStatus.INTERNAL_SERVER_ERROR.name(),
          "Unable to authenticate GitHub user");
    }
  }

  private void validateCallbackRequest(AdminGitHubCallbackRequest callbackRequest) {
    if (callbackRequest == null || StringUtils.isAnyBlank(callbackRequest.getCode(), callbackRequest.getState())) {
      throw new Oauth2ExchangeCodeException(HttpStatus.BAD_REQUEST.name(), "Missing OAuth callback payload");
    }
  }

  private void validateState(HttpSession session, String state) {
    if (session == null) {
      throw new Oauth2ExchangeCodeException(HttpStatus.BAD_REQUEST.name(), "Missing OAuth session");
    }

    Map<String, Instant> pendingStates = getPendingStates(session);
    removeExpiredStates(pendingStates);

    Instant expiry = pendingStates.remove(state);
    session.setAttribute(OAUTH_STATE_SESSION_ATTRIBUTE, pendingStates);

    if (expiry == null || expiry.isBefore(Instant.now())) {
      throw new Oauth2ExchangeCodeException(HttpStatus.BAD_REQUEST.name(), "Invalid OAuth state");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Instant> getPendingStates(HttpSession session) {
    Object attribute = session.getAttribute(OAUTH_STATE_SESSION_ATTRIBUTE);
    if (attribute instanceof Map<?, ?> states) {
      return new HashMap<>((Map<String, Instant>) states);
    }
    return new HashMap<>();
  }

  private void removeExpiredStates(Map<String, Instant> pendingStates) {
    Instant now = Instant.now();
    pendingStates.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isBefore(now));
  }

  private UserInfo toSessionUser(UserInfo authorizedGitHubUser, GithubUser persistedUser) {
    UserInfo sessionUser = new UserInfo();
    sessionUser.setId(persistedUser.getId());
    sessionUser.setGitHubId(persistedUser.getGitHubId());
    sessionUser.setProvider(persistedUser.getProvider());
    sessionUser.setUsername(persistedUser.getUsername());
    sessionUser.setName(persistedUser.getName());
    sessionUser.setAvatarUrl(persistedUser.getAvatarUrl());
    sessionUser.setUrl(authorizedGitHubUser.getUrl());
    sessionUser.setToken(null);
    return sessionUser;
  }
}
