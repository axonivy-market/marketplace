package com.axonivy.market.controller;

import com.axonivy.market.config.SyncTaskCancellationRegistry;
import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(SecurityMonitorController.class)
class SecurityMonitorControllerTest extends WebMvcControllerTestSupport {

  @MockitoBean
  private GitHubService gitHubService;

  @MockitoBean
  private SyncTaskCancellationRegistry cancellationRegistry;

  @Test
  void testSyncGitHubMarketplaceSecurityReturnsServiceResult() throws Exception {
    ProductSecurityInfo info = new ProductSecurityInfo();
    info.setRepoName("portal");
    when(gitHubService.syncSecurityDetailsForProduct()).thenReturn(List.of(info));

    mockMvc.perform(post("/api/security-monitor")
            .with(requestedByHeader()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].repoName").value("portal"));
  }

  @Test
  void testGetGitHubMarketplaceSecurityBuildsCriteriaAndReturnsPagedModel() throws Exception {
    String search = "portal";
    String sort = "dependabotAlerts";
    String sortDirection = "DESC";
    Pageable pageable = PageRequest.of(1, 2);

    ProductSecurityInfo infoA = new ProductSecurityInfo();
    infoA.setRepoName("repo-a");
    ProductSecurityInfo infoB = new ProductSecurityInfo();
    infoB.setRepoName("repo-b");

    Page<ProductSecurityInfo> servicePage = new PageImpl<>(List.of(infoA, infoB), pageable, 9);
    when(gitHubService.searchSecurityDetails(any(ProductSecurityCriteria.class), any())).thenReturn(servicePage);

    mockMvc.perform(get("/api/security-monitor")
            .param("search", search)
            .param("sort", sort)
            .param("sortDirection", sortDirection)
            .param("page", "1")
            .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..repoName").value(hasItem("repo-a")))
        .andExpect(jsonPath("$..repoName").value(hasItem("repo-b")))
        .andExpect(jsonPath("$.page.totalElements").value(9));
  }
}
