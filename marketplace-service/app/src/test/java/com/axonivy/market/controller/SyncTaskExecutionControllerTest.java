package com.axonivy.market.controller;

import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.service.SyncTaskExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(SyncTaskExecutionController.class)
class SyncTaskExecutionControllerTest extends WebMvcControllerTestSupport {

  private static final String JOB_KEY = "jobKey";

  @MockitoBean
  private SyncTaskExecutionService syncTaskExecutionService;

  @Test
  void testGetAllSyncTaskExecutions() throws Exception {
    List<SyncTaskExecutionModel> models = List.of(new SyncTaskExecutionModel());
    when(syncTaskExecutionService.getAllSyncTaskExecutions()).thenReturn(models);

    mockMvc.perform(get("/api/sync-task-execution"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testGetSyncTaskExecutionByKeyFound() throws Exception {
    SyncTaskExecutionModel model = new SyncTaskExecutionModel();
    when(syncTaskExecutionService.getSyncTaskExecutionByKey(JOB_KEY)).thenReturn(model);

    mockMvc.perform(get("/api/sync-task-execution/{jobKey}", JOB_KEY))
        .andExpect(status().isOk());
  }

  @Test
  void testGetSyncTaskExecutionByKeyNotFound() throws Exception {
    when(syncTaskExecutionService.getSyncTaskExecutionByKey(JOB_KEY)).thenReturn(null);

    mockMvc.perform(get("/api/sync-task-execution/{jobKey}", JOB_KEY))
        .andExpect(status().isNotFound());
  }

  @Test
  void testCancelSyncTaskReturnsAcceptedWhenCancelled() throws Exception {
    when(syncTaskExecutionService.cancel(JOB_KEY)).thenReturn(true);

    mockMvc.perform(post("/api/sync-task-execution/{jobKey}/cancel", JOB_KEY)
            .with(requestedByHeader()))
        .andExpect(status().isAccepted());
  }

  @Test
  void testCancelSyncTaskReturnsNotFoundWhenNotCancelled() throws Exception {
    when(syncTaskExecutionService.cancel(JOB_KEY)).thenReturn(false);

    mockMvc.perform(post("/api/sync-task-execution/{jobKey}/cancel", JOB_KEY)
            .with(requestedByHeader()))
        .andExpect(status().isNotFound());
  }
}
