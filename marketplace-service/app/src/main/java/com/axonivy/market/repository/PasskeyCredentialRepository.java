package com.axonivy.market.repository;

import com.axonivy.market.entity.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, String> {
  Optional<PasskeyCredential> findByGithubUserId(String githubUserId);

  Optional<PasskeyCredential> findByCredentialId(String credentialId);

  Optional<PasskeyCredential> findByCredentialIdAndUserHandle(String credentialId, String userHandle);

  List<PasskeyCredential> findAllByGithubUserId(String githubUserId);

  void deleteByGithubUserId(String githubUserId);
}
