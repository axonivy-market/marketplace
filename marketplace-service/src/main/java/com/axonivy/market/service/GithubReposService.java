package com.axonivy.market.service;

public interface GithubReposService {
  String fetchAllRepositories();
  String fetchWorkflowRuns(String repo, String workflow);
}
