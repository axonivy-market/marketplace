package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreGithubRepository;
import org.springframework.stereotype.Repository;

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
