package com.axonivy.market.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
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
import org.springframework.security.web.webauthn.jackson.WebauthnJackson2Module;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;
import org.springframework.security.web.webauthn.registration.HttpSessionPublicKeyCredentialCreationOptionsRepository;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class PasskeyConfig {
  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper(List<Module> modules) {
    JsonMapper.Builder builder = JsonMapper.builder().findAndAddModules();
    modules.forEach(builder::addModule);
    return builder.build();
  }

  @Bean
  @ConditionalOnExpression(
      "T(org.springframework.util.StringUtils).hasText('${market.passkey.rp-id:}')"
          + " and T(org.springframework.util.StringUtils).hasText('${market.passkey.origins:}')")
  public PublicKeyCredentialRpEntity passkeyRelyingParty(
      @Value("${market.passkey.rp-id}") String rpId,
      @Value("${market.passkey.rp-name:Axon Ivy Marketplace Admin}") String rpName) {
    return PublicKeyCredentialRpEntity.builder().id(rpId).name(rpName).build();
  }

  @Bean
  @ConditionalOnExpression(
      "T(org.springframework.util.StringUtils).hasText('${market.passkey.rp-id:}')"
          + " and T(org.springframework.util.StringUtils).hasText('${market.passkey.origins:}')")
  public WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations(
      PublicKeyCredentialUserEntityRepository userEntityRepository,
      UserCredentialRepository userCredentialRepository,
      PublicKeyCredentialRpEntity rpEntity,
      @Value("${market.passkey.origins}") String originsConfig,
      @Value("${market.passkey.timeout-ms:300000}") long timeoutMs) {
    Set<String> origins = Arrays.stream(StringUtils.defaultString(originsConfig).split(","))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet());

    Webauthn4JRelyingPartyOperations operations = new Webauthn4JRelyingPartyOperations(
        userEntityRepository, userCredentialRepository, rpEntity, origins);
    operations.setCustomizeCreationOptions(builder -> customizeCreationOptions(timeoutMs, builder));
    operations.setCustomizeRequestOptions(builder -> customizeRequestOptions(timeoutMs, builder));
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

  @Bean
  public Module webauthnJacksonModule() {
    return new WebauthnJackson2Module();
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
