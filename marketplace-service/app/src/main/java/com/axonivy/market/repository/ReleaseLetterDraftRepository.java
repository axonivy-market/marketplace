package com.axonivy.market.repository;

import com.axonivy.market.entity.ReleaseLetterDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseLetterDraftRepository extends JpaRepository<ReleaseLetterDraft, String> {
  Optional<ReleaseLetterDraft> findByGitHubUserIdAndReleaseLetterId(
      String githubUserId,
      String releaseLetterId
  );

  boolean existsByGitHubUserIdAndReleaseLetterId(
      String githubUserId,
      String releaseLetterId
  );

  void deleteByReleaseLetterId(String releaseLetterId);

  @Modifying
  @Query("DELETE from ReleaseLetterDraft r where r.gitHubUserId = :gitHubUserId AND r.releaseLetterId = " +
      ":releaseLetterId")
  void deleteByGitHubUserIdAndReleaseLetterIdReturningCount(@Param("gitHubUserId") String gitHubUserId,
      @Param("releaseLetterId") String releaseLetterId);
}
