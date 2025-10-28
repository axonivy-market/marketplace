package com.axonivy.market.repository;

import com.axonivy.market.criteria.MonitoringSearchCriteria;
import com.axonivy.market.entity.GithubRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomGithubRepoRepository {
  Page<GithubRepo> findAllByFocusedSorted(MonitoringSearchCriteria criteria, Pageable pageable);
}
