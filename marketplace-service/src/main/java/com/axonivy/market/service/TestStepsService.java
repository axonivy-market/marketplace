package com.axonivy.market.service;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TestStepsService {
  List<TestStep> createTestSteps(JsonNode testData, WorkFlowType workflowType);
  List<TestStepsModel> fetchTestReport(String repo, WorkFlowType type);
}
