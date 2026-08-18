package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.config.SyncTaskCancellationRegistry;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(MonitorDashBoardController.class)
class MonitorDashBoardControllerTest extends WebMvcControllerTestSupport {

  @MockitoBean
  private GithubReposService githubReposService;

  @MockitoBean
  private TestStepsService testStepsService;

  @MockitoBean
  private SyncTaskCancellationRegistry cancellationRegistry;

  @Test
  void testGetTestReportReturnsList() throws Exception {
    TestStepsModel model = new TestStepsModel();
    when(testStepsService.fetchTestReport("repo", WorkFlowType.CI)).thenReturn(List.of(model));

    mockMvc.perform(get("/api/monitor-dashboard/{productId}/{workflow}", "repo", "CI"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
  }

  @Test
  void testSyncGithubMonitorReturnsOk() throws Exception {
    mockMvc.perform(put("/api/monitor-dashboard/sync")
            .with(requestedByHeader()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Repositories loaded successfully."));
  }

  @Test
  void testUpdateRepoPriorities() throws Exception {
    mockMvc.perform(put("/api/monitor-dashboard/focus")
            .with(requestedByHeader())
            .param("repos", "repo1")
            .param("repos", "repo2")
            .param("isFocused", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Focused repository updated successfully."));
  }

  @Test
  void testFindAllFeedbacksReturnPagedModel() throws Exception {
    GithubReposModel model = new GithubReposModel();
    Page<GithubReposModel> page = new PageImpl<>(List.of(model), PageRequest.of(0, 10), 1);
    when(githubReposService.fetchAllRepositories(eq(true), eq("feedback"), eq("name"), eq("ASC"), any(PageRequest.class)))
        .thenReturn(page);

    mockMvc.perform(get("/api/monitor-dashboard/repos")
            .param("isFocused", "true")
            .param("search", "feedback")
            .param("workflowType", "name")
            .param("sortDirection", "ASC")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..repoName").exists());
  }
}
