package com.axonivy.market.core.repository;

import com.axonivy.market.core.criteria.MonitoringSearchCriteria;
import com.axonivy.market.core.entity.GithubRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CoreCustomGithubRepoRepository {
  Page<GithubRepo> findAllByFocusedSorted(MonitoringSearchCriteria criteria, Pageable pageable);
}
