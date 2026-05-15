package com.axonivy.market.repository;

import com.axonivy.market.entity.ReleaseLetter;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseLetterRepository extends JpaRepository<ReleaseLetter, String> {

  @Query("""
      SELECT r
      FROM ReleaseLetter r
      WHERE r.content IS NOT NULL
        AND TRIM(r.content) <> ''
      """)
  Page<ReleaseLetter> findAllWithContent(Pageable pageable);

  boolean existsBySprint(String releaseVersion);

  Page<ReleaseLetter> findByIsLatest(boolean isLatest, Pageable pageable);

  @Modifying
  @Query("DELETE from ReleaseLetter r where r.id = :id")
  int deleteByIdReturningCount(@Param("id") String id);

  @Modifying
  @Transactional
  @Query("UPDATE ReleaseLetter r SET r.isLatest = false WHERE r.isLatest = true AND (r.sprint <> :currentSprint)")
  void deactivateOtherLatestReleaseLetters(String currentSprint);
}
