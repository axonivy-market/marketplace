package com.axonivy.market.service;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.WorkflowRepo;
import com.fasterxml.jackson.databind.JsonNode;

public interface WorkflowRepoProcessor {
  WorkflowRepo createWorkflowRepo(JsonNode testData, GithubRepo repo, String workflowType);
}
