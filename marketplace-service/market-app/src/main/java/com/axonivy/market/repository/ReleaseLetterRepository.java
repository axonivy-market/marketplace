package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.entity.ReleaseLetter;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseLetterRepository extends JpaRepository<ReleaseLetter, String> {
  Optional<ReleaseLetter> findBySprint(String sprint);

  boolean existsBySprint(String releaseVersion);

  Page<ReleaseLetter> findByIsActive(boolean isActive, Pageable pageable);

  void deleteBySprint(String sprint);

  @Modifying
  @Transactional
  @Query("UPDATE ReleaseLetter r SET r.isActive = false WHERE r.isActive = true AND (r.sprint <> :currentSprint)")
  void deactivateOtherActiveReleaseLetters(String currentSprint);
}
