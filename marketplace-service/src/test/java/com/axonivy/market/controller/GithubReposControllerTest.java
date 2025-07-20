package com.axonivy.market.controller;

import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GithubReposControllerTest {
    @Mock
    private GithubReposService githubReposService;
    @Mock
    private TestStepsService testStepsService;
    @InjectMocks
    private GithubReposController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetGitHubReposReturnsList() {
        GithubReposModel model = new GithubReposModel();
        when(githubReposService.fetchAllRepositories()).thenReturn(List.of(model));
        ResponseEntity<List<GithubReposModel>> response = controller.getGitHubRepos();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertSame(model, response.getBody().get(0));
    }

    @Test
    void testGetTestReportReturnsList() {
        TestStepsModel model = new TestStepsModel();
        when(testStepsService.fetchTestReport("repo", WorkFlowType.CI)).thenReturn(List.of(model));
        ResponseEntity<List<TestStepsModel>> response = controller.getTestReport("repo", WorkFlowType.CI);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertSame(model, response.getBody().get(0));
    }

    @Test
    void testSyncGithubMonitorReturnsOk() throws IOException {
        doNothing().when(githubReposService).loadAndStoreTestReports();
        ResponseEntity<String> response = controller.syncGithubMonitor();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Repositories loaded successfully.", response.getBody());
    }

    @Test
    void testSyncGithubMonitorHandlesException() throws IOException {
        doThrow(new IOException("fail")).when(githubReposService).loadAndStoreTestReports();
        assertThrows(IOException.class, () -> controller.syncGithubMonitor());
    }
}