package com.axonivy.market.service;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.model.TestStepsModel;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TestStepsService {
  void createTestSteps(GithubRepo repo, JsonNode testData, String workflowType);
  List<TestStepsModel> fetchTestReport(String repo, String type);
}
