package com.axonivy.market.service;

import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.model.TestStepsModel;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TestStepsService {
  void createNewTestSteps(WorkflowRepo workflow, JsonNode testData);
  List<TestStepsModel> fetchTestReport(String repo, String type);
}
