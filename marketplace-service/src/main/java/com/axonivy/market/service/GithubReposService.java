package com.axonivy.market.service;

import com.axonivy.market.model.RepoFocusedUpdateModel;
import com.axonivy.market.model.ReposResponseModel;

import java.io.IOException;

public interface GithubReposService {
  void loadAndStoreTestReports() throws IOException;

  ReposResponseModel fetchAllRepositories();

  void updateFocusedRepo(RepoFocusedUpdateModel repoPriorityUpdateModels);
}
