package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, String>, CustomGithubRepoRepository {
  @EntityGraph(attributePaths = {"workflowInformation","testSteps"})
  List<GithubRepo> findByNameOrProductId(String name, String productId);

  @Modifying
  @Transactional
  @Query("UPDATE GithubRepo g SET g.focused = :isFocused WHERE g.name IN :names")
  void updateFocusedRepoByName(@Param("names") List<String> names, @Param("isFocused") Boolean isFocused);

}
