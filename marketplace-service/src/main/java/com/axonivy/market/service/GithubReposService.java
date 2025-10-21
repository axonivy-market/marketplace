package com.axonivy.market.service;

import com.axonivy.market.model.GithubReposModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface GithubReposService {
  void loadAndStoreTestReports() throws IOException;

  void updateFocusedRepo(List<String> repos);

  Page<GithubReposModel> fetchAllRepositories(Boolean isFocused, String searchText, String workFlowType,
      String sortDirection, Pageable pageable);

}
