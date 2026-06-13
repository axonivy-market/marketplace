package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.model.PasskeyAssertionOptionsRequest;
import com.axonivy.market.model.PasskeyCredentialRequest;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.service.AdminAuthenticationSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJackson2Module;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialRequestOptionsRequest;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPasskeyServiceImplTest {
  @Mock
  private WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations;
  @Mock
  private PublicKeyCredentialCreationOptionsRepository creationOptionsRepository;
  @Mock
  private PublicKeyCredentialRequestOptionsRepository requestOptionsRepository;
  @Mock
  private GithubUserRepository githubUserRepository;
  @Mock
  private AdminAuthenticationSessionService adminAuthenticationSessionService;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private AdminPasskeyServiceImpl service;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new WebauthnJackson2Module());

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    service = new AdminPasskeyServiceImpl(webAuthnRelyingPartyOperations, creationOptionsRepository,
        requestOptionsRepository, githubUserRepository, adminAuthenticationSessionService, objectMapper);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void beginRegistrationStoresChallengeInRepository() {
    UserInfo currentUser = new UserInfo();
    currentUser.setId("user-1");
    currentUser.setUsername("octopus");

    GithubUser githubUser = new GithubUser();
    githubUser.setId("user-1");
    githubUser.setUsername("octopus");
    githubUser.setName("Octopus");

    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated(currentUser, null,
            AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

    PublicKeyCredentialCreationOptions options = PublicKeyCredentialCreationOptions.builder()
        .rp(PublicKeyCredentialRpEntity.builder().id("localhost").name("Marketplace").build())
        .user(ImmutablePublicKeyCredentialUserEntity.builder()
            .id(new Bytes("user-1".getBytes()))
            .name("octopus")
            .displayName("Octopus")
            .build())
        .challenge(new Bytes(new byte[] {1, 2, 3}))
        .pubKeyCredParams(List.of())
        .build();

    when(githubUserRepository.findById("user-1")).thenReturn(Optional.of(githubUser));
    when(webAuthnRelyingPartyOperations.createPublicKeyCredentialCreationOptions(any())).thenReturn(options);

    Map<String, Object> result = service.beginRegistration(currentUser, request, response);

    assertNotNull(result);
    assertNotNull(result.get("challenge"));
    verify(creationOptionsRepository).save(request, response, options);
  }

  @Test
  void beginAuthenticationStoresRequestOptions() {
    PublicKeyCredentialRequestOptions options = PublicKeyCredentialRequestOptions.builder()
        .challenge(new Bytes(new byte[] {1, 2, 3}))
        .rpId("localhost")
        .build();
    when(webAuthnRelyingPartyOperations.createCredentialRequestOptions(any())).thenReturn(options);

    PasskeyAssertionOptionsRequest optionsRequest = new PasskeyAssertionOptionsRequest();
    optionsRequest.setUsername("octopus");

    Map<String, Object> result = service.beginAuthentication(optionsRequest, request, response);

    assertEquals("localhost", result.get("rpId"));
    verify(requestOptionsRepository).save(request, response, options);

    ArgumentCaptor<PublicKeyCredentialRequestOptionsRequest> captor =
        ArgumentCaptor.forClass(PublicKeyCredentialRequestOptionsRequest.class);
    verify(webAuthnRelyingPartyOperations).createCredentialRequestOptions(captor.capture());
    assertEquals("octopus", captor.getValue().getAuthentication().getName());
  }

  @Test
  void finishAuthenticationFailsWhenChallengeMissing() {
    when(requestOptionsRepository.load(request)).thenReturn(null);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.finishAuthentication(new PasskeyCredentialRequest(), request, response));

    assertEquals(400, exception.getStatusCode().value());
    assertEquals("Missing passkey challenge", exception.getReason());
  }

  @Test
  void finishRegistrationFailsWhenCredentialMissing() {
    UserInfo currentUser = new UserInfo();
    currentUser.setId("user-1");
    GithubUser githubUser = new GithubUser();
    githubUser.setId("user-1");
    githubUser.setUsername("octopus");

    when(githubUserRepository.findById("user-1")).thenReturn(Optional.of(githubUser));
    when(creationOptionsRepository.load(request)).thenReturn(PublicKeyCredentialCreationOptions.builder()
        .rp(PublicKeyCredentialRpEntity.builder().id("localhost").name("Marketplace").build())
        .user(ImmutablePublicKeyCredentialUserEntity.builder()
            .id(new Bytes("user-1".getBytes()))
            .name("octopus")
            .displayName("Octopus")
            .build())
        .challenge(new Bytes(new byte[] {1, 2, 3}))
        .pubKeyCredParams(List.of())
        .build());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.finishRegistration(currentUser, new PasskeyCredentialRequest(), request));

    assertEquals(400, exception.getStatusCode().value());
    assertEquals("Missing passkey credential payload", exception.getReason());
  }
}
