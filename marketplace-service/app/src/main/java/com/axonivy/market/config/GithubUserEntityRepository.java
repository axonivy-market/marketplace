package com.axonivy.market.config;

import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.repository.GithubUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GithubUserEntityRepository implements PublicKeyCredentialUserEntityRepository {
  private final GithubUserRepository githubUserRepository;

  @Override
  public PublicKeyCredentialUserEntity findById(Bytes id) {
    return githubUserRepository.findById(toGithubUserId(id)).map(this::toEntity).orElse(null);
  }

  @Override
  public PublicKeyCredentialUserEntity findByUsername(String username) {
    return githubUserRepository.findByUsernameIgnoreCase(username).map(this::toEntity).orElse(null);
  }

  @Override
  public void save(PublicKeyCredentialUserEntity userEntity) {
    GithubUser githubUser = githubUserRepository.findByUsernameIgnoreCase(userEntity.getName())
        .orElseThrow(() -> new IllegalArgumentException("GitHub user not found for passkey registration"));

    if (!toGithubUserId(userEntity.getId()).equals(githubUser.getId())) {
      throw new IllegalArgumentException("Passkey user id mismatch");
    }
  }

  @Override
  public void delete(Bytes id) {
    // GithubUser lifecycle is managed outside of WebAuthn.
  }

  private PublicKeyCredentialUserEntity toEntity(GithubUser githubUser) {
    return ImmutablePublicKeyCredentialUserEntity.builder()
        .id(new Bytes(githubUser.getId().getBytes(StandardCharsets.UTF_8)))
        .name(githubUser.getUsername())
        .displayName(StringUtils.defaultIfBlank(githubUser.getName(), githubUser.getUsername()))
        .build();
  }

  private String toGithubUserId(Bytes id) {
    return new String(id.getBytes(), StandardCharsets.UTF_8);
  }
}
