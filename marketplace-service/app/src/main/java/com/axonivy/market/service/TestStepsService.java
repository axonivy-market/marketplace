package com.axonivy.market.service;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TestStepsService {
  
  /**
   * <p>
   * Create test steps
   * </p>
   *
   * @param  testData
   *              type {@link JsonNode}
   * @param  workflowType
   *              type {@link WorkFlowType}
   * @return {@link List<TestStep>}
   * @author ttan
   */
  List<TestStep> createTestSteps(JsonNode testData, WorkFlowType workflowType);

  /**
   * <p>
   * Fetch all test report
   * </p>
   *
   * @param  repo
   *              type {@link String}
   * @param  type
   *              type {@link WorkFlowType}
   * @return {@link List<TestStepsModel>}
   * @author ttan
   */
  List<TestStepsModel> fetchTestReport(String repo, WorkFlowType type);
}
