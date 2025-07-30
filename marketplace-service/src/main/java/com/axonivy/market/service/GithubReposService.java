package com.axonivy.market.service;

import com.axonivy.market.model.RepoPremiumUpdateModel;
import com.axonivy.market.model.ReposResponseModel;

import java.io.IOException;

public interface GithubReposService {
  void loadAndStoreTestReports() throws IOException;

  ReposResponseModel fetchRepositories();

  void updateRepoPremium(RepoPremiumUpdateModel repoPriorityUpdateModels);
}
