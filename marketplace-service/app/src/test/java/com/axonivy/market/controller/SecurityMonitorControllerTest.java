package com.axonivy.market.controller;

import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.ProductSecuritySortOption;
import com.axonivy.market.github.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityMonitorControllerTest {

  @Mock
  private GitHubService gitHubService;

  @InjectMocks
  private SecurityMonitorController controller;

  @Test
  void testSyncGitHubMarketplaceSecurityReturnsServiceResult() throws IOException {
    ProductSecurityInfo info = new ProductSecurityInfo();
    info.setRepoName("portal");
    List<ProductSecurityInfo> expected = List.of(info);
    when(gitHubService.syncSecurityDetailsForProduct()).thenReturn(expected);

    ResponseEntity<List<ProductSecurityInfo>> response = controller.syncGitHubMarketplaceSecurity();

    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals(1, response.getBody().size(), "Response body should contain exactly 1 item");
    assertEquals("portal", response.getBody().getFirst().getRepoName(), "First repo name should be 'portal'");
    verify(gitHubService).syncSecurityDetailsForProduct();
  }

  @Test
  void testGetGitHubMarketplaceSecurityBuildsCriteriaAndReturnsPagedModel() throws IOException {
    String search = "portal";
    String sort = "dependabotAlerts";
    String sortDirection = "DESC";
    Pageable pageable = PageRequest.of(1, 2);

    ProductSecurityInfo infoA = new ProductSecurityInfo();
    infoA.setRepoName("repo-a");
    ProductSecurityInfo infoB = new ProductSecurityInfo();
    infoB.setRepoName("repo-b");

    Page<ProductSecurityInfo> servicePage = new PageImpl<>(List.of(infoA, infoB), pageable, 9);
    when(gitHubService.searchSecurityDetails(org.mockito.ArgumentMatchers.any(ProductSecurityCriteria.class),
        org.mockito.ArgumentMatchers.eq(pageable))).thenReturn(servicePage);

    ResponseEntity<PagedModel<ProductSecurityInfo>> response =
        controller.getGitHubMarketplaceSecurity(search, sort, sortDirection, pageable);

    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals(2, response.getBody().getContent().size(), "Response body content should contain 2 items");
    assertNotNull(response.getBody().getMetadata(), "Response metadata should not be null");
    assertEquals(2, response.getBody().getMetadata().getSize(), "Metadata size should be 2");
    assertEquals(1, response.getBody().getMetadata().getNumber(), "Metadata page number should be 1");
    assertEquals(9, response.getBody().getMetadata().getTotalElements(), "Metadata total elements should be 9");
    assertEquals(5, response.getBody().getMetadata().getTotalPages(), "Metadata total pages should be 5");

    ArgumentCaptor<ProductSecurityCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSecurityCriteria.class);
    verify(gitHubService).searchSecurityDetails(criteriaCaptor.capture(), org.mockito.ArgumentMatchers.eq(pageable));

    ProductSecurityCriteria captured = criteriaCaptor.getValue();
    assertNotNull(captured, "Captured criteria should not be null");
    assertEquals(search, captured.getSearchText(), "Captured search text should match input search");
    assertEquals(ProductSecuritySortOption.DEPENDABOT_ALERTS, captured.getSortOption(),
        "Sort option should be DEPENDABOT_ALERTS");
    assertEquals(sortDirection, captured.getSortDirection(), "Sort direction should match input direction");
  }
}
