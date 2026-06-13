package com.axonivy.market.config;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.entity.PasskeyCredential;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.repository.PasskeyCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GithubUserCredentialRepository implements UserCredentialRepository {
  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final GithubUserRepository githubUserRepository;

  @Override
  public void delete(Bytes userId) {
    passkeyCredentialRepository.deleteByGithubUserId(toGithubUserId(userId));
  }

  @Override
  public void save(CredentialRecord credentialRecord) {
    String githubUserId = toGithubUserId(credentialRecord.getUserEntityUserId());
    GithubUser githubUser = githubUserRepository.findById(githubUserId)
        .orElseThrow(() -> new IllegalArgumentException("Passkey user not found: " + githubUserId));

    PasskeyCredential credential = passkeyCredentialRepository.findByGithubUserId(githubUserId)
        .orElseGet(PasskeyCredential::new);
    credential.setGithubUserId(githubUserId);
    credential.setCredentialId(credentialRecord.getCredentialId().toBase64UrlString());
    credential.setUserHandle(credentialRecord.getUserEntityUserId().toBase64UrlString());
    credential.setPublicKeyCose(java.util.Base64.getUrlEncoder().withoutPadding()
        .encodeToString(credentialRecord.getPublicKey().getBytes()));
    credential.setSignatureCount(credentialRecord.getSignatureCount());
    credential.setCredentialType(credentialRecord.getCredentialType().getValue());
    credential.setUvInitialized(credentialRecord.isUvInitialized());
    credential.setTransports(serializeTransports(credentialRecord.getTransports()));
    credential.setBackupEligible(credentialRecord.isBackupEligible());
    credential.setBackupState(credentialRecord.isBackupState());
    credential.setAttestationObject(credentialRecord.getAttestationObject().toBase64UrlString());
    credential.setAttestationClientDataJson(credentialRecord.getAttestationClientDataJSON().toBase64UrlString());
    credential.setLabel(StringUtils.defaultIfBlank(credentialRecord.getLabel(),
        StringUtils.defaultIfBlank(githubUser.getUsername(), "Admin passkey")));
    credential.setCreated(Optional.ofNullable(credentialRecord.getCreated()).orElseGet(Instant::now));
    credential.setLastUsed(Optional.ofNullable(credentialRecord.getLastUsed()).orElseGet(Instant::now));
    passkeyCredentialRepository.save(credential);
  }

  @Override
  public CredentialRecord findByCredentialId(Bytes credentialId) {
    return passkeyCredentialRepository.findByCredentialId(credentialId.toBase64UrlString())
        .map(this::toCredentialRecord)
        .orElse(null);
  }

  @Override
  public List<CredentialRecord> findByUserId(Bytes userId) {
    return passkeyCredentialRepository.findAllByGithubUserId(toGithubUserId(userId)).stream()
        .map(this::toCredentialRecord)
        .toList();
  }

  private CredentialRecord toCredentialRecord(PasskeyCredential credential) {
    return ImmutableCredentialRecord.builder()
        .credentialType(PublicKeyCredentialType.valueOf(credential.getCredentialType()))
        .credentialId(Bytes.fromBase64(credential.getCredentialId()))
        .userEntityUserId(Bytes.fromBase64(credential.getUserHandle()))
        .publicKey(ImmutablePublicKeyCose.fromBase64(credential.getPublicKeyCose()))
        .signatureCount(credential.getSignatureCount())
        .uvInitialized(credential.isUvInitialized())
        .transports(parseTransports(credential.getTransports()))
        .backupEligible(credential.isBackupEligible())
        .backupState(credential.isBackupState())
        .attestationObject(Bytes.fromBase64(credential.getAttestationObject()))
        .attestationClientDataJSON(Bytes.fromBase64(credential.getAttestationClientDataJson()))
        .label(credential.getLabel())
        .created(credential.getCreated())
        .lastUsed(credential.getLastUsed())
        .build();
  }

  private String serializeTransports(Set<AuthenticatorTransport> transports) {
    if (transports == null || transports.isEmpty()) {
      return "";
    }
    return transports.stream().map(AuthenticatorTransport::getValue).sorted().collect(Collectors.joining(","));
  }

  private Set<AuthenticatorTransport> parseTransports(String transports) {
    if (StringUtils.isBlank(transports)) {
      return Collections.emptySet();
    }
    return Arrays.stream(transports.split(","))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .map(AuthenticatorTransport::valueOf)
        .collect(Collectors.toSet());
  }

  private String toGithubUserId(Bytes userId) {
    return new String(userId.getBytes(), StandardCharsets.UTF_8);
  }
}
