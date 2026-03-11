package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreGithubRepository;
import com.axonivy.market.criteria.MonitoringSearchCriteria;
import com.axonivy.market.entity.GithubRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubRepoRepository extends CoreGithubRepository, CustomGithubRepoRepository {
//  @EntityGraph(attributePaths = {"workflowInformation","testSteps"})
//  List<GithubRepo> findByNameOrProductId(String name, String productId);

//  @Modifying
//  @Transactional
//  @Query("UPDATE GithubRepo g SET g.focused = true WHERE g.name IN :names")
//  void updateFocusedRepoByName(List<String> names);

//  @Override
//  default Page<GithubRepo> findAllByFocusedSorted(MonitoringSearchCriteria criteria, Pageable pageable) {
//    return null;
//  }
}
