package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, String> {
  GithubRepo findByName(String name);

  @Modifying
  @Transactional
  @Query("UPDATE GithubRepo g SET g.focused = true WHERE g.name IN :names")
  void updateFocusedRepoByName(List<String> names);

  @Query("SELECT r FROM GithubRepo r LEFT JOIN FETCH r.testSteps WHERE r.name = :name")
  Optional<GithubRepo> findByNameWithTestSteps(@Param("name") String name);
}
