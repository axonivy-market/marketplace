package com.axonivy.market.service;

import com.axonivy.market.model.GithubReposModel;

import java.io.IOException;
import java.util.List;

public interface GithubReposService {
  void loadAndStoreTestReports() throws IOException;

  List<GithubReposModel> fetchAllRepositories();

  void updateFocusedRepo(List<String> repos);
}
