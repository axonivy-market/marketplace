package com.axonivy.market.service;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TestStepsService {
  
  /**
   * <p>
   * Creates test step records from test data JSON. Parses workflow test information and creates
   * TestStep entities for different workflow types (build, deploy, test). Each test step represents
   * a single test execution within a workflow.
   * </p>
   *
   * @param  testData
   *              type {@link JsonNode} - JSON structure containing test step details, execution status,
   *              and results from GitHub workflow runs
   * @param  workflowType
   *              type {@link WorkFlowType} - the type of workflow (BUILD, DEPLOY, TEST, etc.) these steps belong to
   * @return {@link List<TestStep>} - list of created TestStep entities with parsed test data and status;
   *         returns empty list if test data is invalid or empty
   * @author ttan
   */
  List<TestStep> createTestSteps(JsonNode testData, WorkFlowType workflowType);

  /**
   * <p>
   * Retrieves all test report results for a specific GitHub repository and workflow type. Returns compiled
   * test step data with execution results, duration, status (passed/failed/skipped), and logs for CI/CD
   * pipeline monitoring.
   * </p>
   *
   * @param  repo
   *              type {@link String} - the GitHub repository name to fetch test reports from
   * @param  type
   *              type {@link WorkFlowType} - the workflow type to filter test reports by (BUILD, DEPLOY, TEST, etc.)
   * @return {@link List<TestStepsModel>} - list of test step models with execution results and metadata;
   *         returns empty list if no test reports found for the repository/workflow type combination
   * @author ttan
   */
  List<TestStepsModel> fetchTestReport(String repo, WorkFlowType type);
}
