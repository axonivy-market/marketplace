package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AdminAuthenticationSessionServiceImplTest {
  @Mock
  private SessionAuthenticationStrategy sessionAuthenticationStrategy;
  @Mock
  private SecurityContextRepository securityContextRepository;
  @Mock
  private FindByIndexNameSessionRepository<? extends Session> sessionRepository;
  @Mock
  private PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository;
  @Mock
  private UserCredentialRepository userCredentialRepository;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private AdminAuthenticationSessionServiceImpl service;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createSessionBuildsPrincipalAndSavesSecurityContext() {
    GithubUser githubUser = new GithubUser();
    githubUser.setId("user-1");
    githubUser.setGitHubId("gh-1");
    githubUser.setProvider("GitHub");
    githubUser.setUsername("octopus");
    githubUser.setName("Octopus");
    githubUser.setAvatarUrl("https://avatar");

    when(publicKeyCredentialUserEntityRepository.findByUsername("octopus"))
        .thenReturn(ImmutablePublicKeyCredentialUserEntity.builder()
            .id(new Bytes("user-1".getBytes(StandardCharsets.UTF_8)))
            .name("octopus")
            .displayName("Octopus")
            .build());
    when(sessionRepository.findByIndexNameAndIndexValue(
        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, "octopus"))
        .thenReturn(java.util.Map.of(
            "old-session-1", mock(Session.class),
            "old-session-2", mock(Session.class)));
    when(userCredentialRepository.findByUserId(new Bytes("user-1".getBytes(StandardCharsets.UTF_8))))
        .thenReturn(List.of(org.mockito.Mockito.mock(CredentialRecord.class)));

    UserInfo result = service.createSession(githubUser, null, request, response);

    assertEquals("user-1", result.getId());
    assertEquals("gh-1", result.getGitHubId());
    assertEquals("octopus", result.getUsername());
    assertEquals("https://github.com/octopus", result.getUrl());
    assertTrue(result.isHasPasskey());

    verify(sessionAuthenticationStrategy).onAuthentication(any(), any(), any());
    verify(sessionRepository).deleteById("old-session-1");
    verify(sessionRepository).deleteById("old-session-2");
    verify(securityContextRepository).saveContext(any(), any(), any());
  }
}
