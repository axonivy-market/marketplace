package com.axonivy.market.repository;

import com.axonivy.market.entity.ReleaseLetterDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseLetterDraftRepository extends JpaRepository<ReleaseLetterDraft, String> {
  Optional<ReleaseLetterDraft> findByGitHubUserIdAndReleaseLetterId(
      String githubUserId,
      String releaseLetterId
  );

  void deleteByReleaseLetterId(String releaseLetterId);
}
