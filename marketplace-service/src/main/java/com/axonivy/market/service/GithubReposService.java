package com.axonivy.market.service;

import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.RepoPriorityUpdateModel;

import java.io.IOException;
import java.util.List;

public interface GithubReposService {
  void loadAndStoreTestReports() throws IOException;

  List<GithubReposModel> fetchFocusRepositories();

  List<GithubReposModel> fetchStandardRepositories();

  void updateRepoPriority(List<RepoPriorityUpdateModel> repoPriorityUpdateModels);
}
