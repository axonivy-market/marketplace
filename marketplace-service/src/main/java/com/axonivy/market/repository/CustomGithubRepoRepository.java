package com.axonivy.market.repository;

import com.axonivy.market.entity.GithubRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomGithubRepoRepository {
  Page<GithubRepo> findAllByFocusedSorted(Boolean isFocused, String workflowType, String sortDirection, String productId,
      Pageable pageable);
}
