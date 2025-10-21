package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, String>, CustomGithubRepoRepository {
  @EntityGraph(attributePaths = {"workflowInformation","testSteps"})
  GithubRepo findByNameOrProductId(String name, String productId);

  @Modifying
  @Transactional
  @Query("UPDATE GithubRepo g SET g.focused = true WHERE g.name IN :names")
  void updateFocusedRepoByName(List<String> names);

  @Query("SELECT r FROM GithubRepo r LEFT JOIN FETCH r.testSteps WHERE r.name = :name")
  Optional<GithubRepo> findByNameWithTestSteps(@Param("name") String name);

  @Query(value = """
          SELECT r.*
          FROM github_repo r
          LEFT JOIN (
              SELECT w1.*
              FROM workflow_information w1
              WHERE w1.workflow_type = :workflowType
              AND w1.last_built = (
                  SELECT MAX(w2.last_built)
                  FROM workflow_information w2
                  WHERE w2.repository_id = w1.repository_id AND w2.workflow_type = w1.workflow_type
              )
          ) w ON w.repository_id = r.id
          WHERE r.focused = :isFocused
          ORDER BY
            CASE w.conclusion
              WHEN 'success' THEN 1
              WHEN 'failure' THEN 2
              ELSE 3
            END,
            r.id
      """,
      countQuery = "SELECT count(*) FROM github_repo r",
      nativeQuery = true
  )
  Page<GithubRepo> findAllByFocusedAsc(Boolean isFocused, String workflowType,
      Pageable pageable);

  @Query(value = """
          SELECT r.*
          FROM github_repo r
          LEFT JOIN (
              SELECT w1.*
              FROM workflow_information w1
              WHERE w1.workflow_type = :workflowType
              AND w1.last_built = (
                  SELECT MAX(w2.last_built)
                  FROM workflow_information w2
                  WHERE w2.repository_id = w1.repository_id AND w2.workflow_type = w1.workflow_type
              )
          ) w ON w.repository_id = r.id
          WHERE r.focused = :isFocused
          ORDER BY
            CASE w.conclusion
              WHEN 'success' THEN 2
              WHEN 'failure' THEN 1
              ELSE 3
            END,
            r.id
      """,
      countQuery = "SELECT count(*) FROM github_repo r",
      nativeQuery = true
  )
  Page<GithubRepo> findAllByFocusedDESC(Boolean isFocused, String workflowType,
      Pageable pageable);

  @EntityGraph(attributePaths = {"workflowInformation", "testSteps"})
  Page<GithubRepo> findAllByFocusedAndProductIdContainingIgnoreCase(Boolean isFocused, String productId,
      Pageable pageable);
}
