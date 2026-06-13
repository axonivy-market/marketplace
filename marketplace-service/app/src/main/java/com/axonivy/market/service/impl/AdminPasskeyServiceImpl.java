package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.model.PasskeyAssertionOptionsRequest;
import com.axonivy.market.model.PasskeyCredentialRequest;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.service.AdminAuthenticationSessionService;
import com.axonivy.market.service.AdminPasskeyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.webauthn.api.AuthenticatorResponse;
import org.springframework.security.web.webauthn.api.AuthenticatorAssertionResponse;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialCreationOptionsRequest;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialRequestOptionsRequest;
import org.springframework.security.web.webauthn.management.ImmutableRelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.RelyingPartyAuthenticationRequest;
import org.springframework.security.web.webauthn.management.RelyingPartyPublicKey;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnBean(WebAuthnRelyingPartyOperations.class)
public class AdminPasskeyServiceImpl implements AdminPasskeyService {
  private static final String PASSKEY_PROFILE_URL_PREFIX = "https://github.com/";
  private static final String DEFAULT_PASSKEY_LABEL = "Admin passkey";
  private static final String ANONYMOUS_KEY = "passkey-anonymous";

  private final WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations;
  private final PublicKeyCredentialCreationOptionsRepository creationOptionsRepository;
  private final PublicKeyCredentialRequestOptionsRepository requestOptionsRepository;
  private final ObjectMapper objectMapper;
  private final GithubUserRepository githubUserRepository;
  private final AdminAuthenticationSessionService adminAuthenticationSessionService;

  @Override
  public JsonNode beginRegistration(UserInfo currentUser, HttpServletRequest request, HttpServletResponse response) {
    GithubUser githubUser = requireGithubUser(currentUser == null ? null : currentUser.getId());
    Authentication authentication = requireAuthenticatedPrincipal(githubUser.getUsername());

    PublicKeyCredentialCreationOptions options = webAuthnRelyingPartyOperations
        .createPublicKeyCredentialCreationOptions(new ImmutablePublicKeyCredentialCreationOptionsRequest(authentication));
    creationOptionsRepository.save(request, response, options);
    return objectMapper.valueToTree(options);
  }

  @Override
  public UserInfo finishRegistration(UserInfo currentUser, PasskeyCredentialRequest credentialRequest,
      HttpServletRequest request) {
    GithubUser githubUser = requireGithubUser(currentUser == null ? null : currentUser.getId());
    PublicKeyCredentialCreationOptions creationOptions = creationOptionsRepository.load(request);
    if (creationOptions == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing passkey challenge");
    }
    validateCredentialPayload(credentialRequest);

    try {
      PublicKeyCredential<AuthenticatorAttestationResponse> credential =
          readCredential(credentialRequest.getCredential(), AuthenticatorAttestationResponse.class);
      webAuthnRelyingPartyOperations.registerCredential(
          new ImmutableRelyingPartyRegistrationRequest(creationOptions,
              new RelyingPartyPublicKey(credential, DEFAULT_PASSKEY_LABEL)));
      updateSessionPrincipalHasPasskey();
      log.info("Passkey registration success userId={} username={}", githubUser.getId(), githubUser.getUsername());
      return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (Exception exception) {
      log.warn("Passkey registration failed userId={}", githubUser.getId(), exception);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passkey registration failed", exception);
    }
  }

  @Override
  public JsonNode beginAuthentication(PasskeyAssertionOptionsRequest optionsRequest, HttpServletRequest request,
      HttpServletResponse response) {
    Authentication authentication = createAuthenticationRequest(optionsRequest);
    PublicKeyCredentialRequestOptions options = webAuthnRelyingPartyOperations
        .createCredentialRequestOptions(new ImmutablePublicKeyCredentialRequestOptionsRequest(authentication));
    requestOptionsRepository.save(request, response, options);
    return objectMapper.valueToTree(options);
  }

  @Override
  public UserInfo finishAuthentication(PasskeyCredentialRequest credentialRequest, HttpServletRequest request,
      HttpServletResponse response) {
    PublicKeyCredentialRequestOptions requestOptions = requestOptionsRepository.load(request);
    if (requestOptions == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing passkey challenge");
    }
    validateCredentialPayload(credentialRequest);

    try {
      PublicKeyCredential<AuthenticatorAssertionResponse> credential =
          readCredential(credentialRequest.getCredential(), AuthenticatorAssertionResponse.class);
      PublicKeyCredentialUserEntity userEntity = webAuthnRelyingPartyOperations.authenticate(
          new RelyingPartyAuthenticationRequest(requestOptions, credential));

      GithubUser githubUser = resolveAuthenticatedGithubUser(userEntity);
      log.info("Passkey login success userId={} username={}", githubUser.getId(), githubUser.getUsername());
      return adminAuthenticationSessionService.createSession(githubUser,
          PASSKEY_PROFILE_URL_PREFIX + githubUser.getUsername(), request, response);
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (Exception exception) {
      log.warn("Passkey login failed", exception);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passkey authentication failed", exception);
    }
  }

  private Authentication requireAuthenticatedPrincipal(String username) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || StringUtils.isBlank(authentication.getName())) {
      return UsernamePasswordAuthenticationToken.authenticated(username, null,
          AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
    }
    return authentication;
  }

  private Authentication createAuthenticationRequest(PasskeyAssertionOptionsRequest optionsRequest) {
    String username = optionsRequest == null ? null : StringUtils.trimToNull(optionsRequest.getUsername());
    if (username == null) {
      return new AnonymousAuthenticationToken(ANONYMOUS_KEY, "anonymous",
          AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }
    return UsernamePasswordAuthenticationToken.authenticated(username, null, AuthorityUtils.NO_AUTHORITIES);
  }

  private GithubUser requireGithubUser(String githubUserId) {
    if (StringUtils.isBlank(githubUserId)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }
    return githubUserRepository.findById(githubUserId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
  }

  private GithubUser resolveAuthenticatedGithubUser(PublicKeyCredentialUserEntity userEntity) {
    if (userEntity == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passkey user not resolved");
    }
    String githubUserId = new String(userEntity.getId().getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    return githubUserRepository.findById(githubUserId)
        .orElseGet(() -> githubUserRepository.findByUsernameIgnoreCase(userEntity.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passkey user not found")));
  }

  private void updateSessionPrincipalHasPasskey() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserInfo userInfo) {
      userInfo.setHasPasskey(true);
    }
  }

  private void validateCredentialPayload(PasskeyCredentialRequest credentialRequest) {
    if (credentialRequest == null || credentialRequest.getCredential() == null || credentialRequest.getCredential().isNull()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing passkey credential payload");
    }
  }

  private <T extends AuthenticatorResponse> PublicKeyCredential<T> readCredential(JsonNode credentialNode,
      Class<T> responseType) {
    try {
      JavaType credentialType = objectMapper.getTypeFactory()
          .constructParametricType(PublicKeyCredential.class, responseType);
      return objectMapper.readValue(objectMapper.writeValueAsBytes(credentialNode), credentialType);
    } catch (java.io.IOException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid passkey credential payload", exception);
    }
  }
}
