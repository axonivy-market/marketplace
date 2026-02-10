package com.axonivy.market.repository;

import com.axonivy.market.entity.ReleaseLetter;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseLetterRepository extends JpaRepository<ReleaseLetter, String> {
  Optional<ReleaseLetter> findBySprint(String sprint);

  boolean existsBySprint(String releaseVersion);

  @Modifying
  @Transactional
  @Query("UPDATE ReleaseLetter r SET r.isActive = false WHERE r.isActive = true AND (r.sprint <> :currentSprint)")
  void deactivateOtherActiveReleaseLetters(String currentSprint);
}
