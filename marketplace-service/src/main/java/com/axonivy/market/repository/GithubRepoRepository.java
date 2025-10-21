package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, String> {
  @EntityGraph(attributePaths = {"workflowInformation","testSteps"})
  GithubRepo findByNameOrProductId(String name, String productId);

  @Modifying
  @Transactional
  @Query("UPDATE GithubRepo g SET g.focused = true WHERE g.name IN :names")
  void updateFocusedRepoByName(List<String> names);

  @Query("SELECT r FROM GithubRepo r LEFT JOIN FETCH r.testSteps WHERE r.name = :name")
  Optional<GithubRepo> findByNameWithTestSteps(@Param("name") String name);

  @EntityGraph(attributePaths = {"workflowInformation", "testSteps"})
  Page<GithubRepo> findAllByFocused(Boolean isFocused, Pageable pageable);

  @EntityGraph(attributePaths = {"workflowInformation", "testSteps"})
  Page<GithubRepo> findAllByFocusedAndProductIdContainingIgnoreCase(Boolean isFocused, String productId,
      Pageable pageable);
}
