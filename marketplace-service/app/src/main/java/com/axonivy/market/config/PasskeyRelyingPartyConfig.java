package com.axonivy.market.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.api.AttestationConveyancePreference;
import org.springframework.security.web.webauthn.api.AuthenticatorSelectionCriteria;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.api.ResidentKeyRequirement;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.security.web.webauthn.authentication.HttpSessionPublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;
import org.springframework.security.web.webauthn.registration.HttpSessionPublicKeyCredentialCreationOptionsRepository;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@Conditional(PasskeyCondition.class)
public class PasskeyRelyingPartyConfig {
  @Bean
  public PublicKeyCredentialRpEntity passkeyRelyingParty(PasskeyProperties properties) {
    return PublicKeyCredentialRpEntity.builder()
        .id(properties.getRpId())
        .name(StringUtils.defaultIfBlank(properties.getRpName(), "Axon Ivy Marketplace Admin"))
        .build();
  }

  @Bean
  public WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations(
      PublicKeyCredentialUserEntityRepository userEntityRepository,
      UserCredentialRepository userCredentialRepository,
      PublicKeyCredentialRpEntity rpEntity,
      PasskeyProperties properties) {
    Set<String> origins = Arrays.stream(StringUtils.defaultString(properties.getOrigins()).split(","))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet());

    Webauthn4JRelyingPartyOperations operations = new Webauthn4JRelyingPartyOperations(
        userEntityRepository, userCredentialRepository, rpEntity, origins);
    operations.setCustomizeCreationOptions(
        builder -> customizeCreationOptions(properties.getTimeoutMs(), builder));
    operations.setCustomizeRequestOptions(
        builder -> customizeRequestOptions(properties.getTimeoutMs(), builder));
    return operations;
  }

  @Bean
  public PublicKeyCredentialCreationOptionsRepository publicKeyCredentialCreationOptionsRepository() {
    return new HttpSessionPublicKeyCredentialCreationOptionsRepository();
  }

  @Bean
  public PublicKeyCredentialRequestOptionsRepository publicKeyCredentialRequestOptionsRepository() {
    return new HttpSessionPublicKeyCredentialRequestOptionsRepository();
  }

  private void customizeCreationOptions(long timeoutMs,
      PublicKeyCredentialCreationOptions.PublicKeyCredentialCreationOptionsBuilder builder) {
    builder.timeout(Duration.ofMillis(timeoutMs))
        .attestation(AttestationConveyancePreference.NONE)
        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
            .residentKey(ResidentKeyRequirement.PREFERRED)
            .userVerification(UserVerificationRequirement.REQUIRED)
            .build());
  }

  private void customizeRequestOptions(long timeoutMs,
      PublicKeyCredentialRequestOptions.PublicKeyCredentialRequestOptionsBuilder builder) {
    builder.timeout(Duration.ofMillis(timeoutMs))
        .userVerification(UserVerificationRequirement.REQUIRED);
  }
}
