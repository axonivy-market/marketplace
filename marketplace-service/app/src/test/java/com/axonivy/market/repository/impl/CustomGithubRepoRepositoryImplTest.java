package com.axonivy.market.repository.impl;

import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.criteria.MonitoringSearchCriteria;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.repository.GithubRepoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MarketplaceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
public class CustomGithubRepoRepositoryImplTest {
  private static final String PORTAL_PRODUCT_ID = "portal";
  private static final String CONNECTIVITY_PRODUCT_ID = "connectivity-demo";
  private static final String MICROSOFT_365_PRODUCT_ID = "microsoft-365";
  private static final String JIRA_CONNECTOR_PRODUCT_ID = "jira-connector";
  private static final String SLACK_CONNECTOR_PRODUCT_ID = "slack-connector";
  private static final String ARCHIVED_DEMO_PRODUCT_ID = "archived-demo";
  private static final String PORTAL_REPO_ID = "repo-portal";
  private static final String PORTAL_HTML_URL = "https://github.com/axonivy-market/portal";
  private static final String CI_WORKFLOW_TYPE = "CI";
  private static final String ASCENDING = "ASC";
  private static final String DESCENDING = "DESC";
  private static final String UPPERCASE_CONNECTOR_SEARCH_TEXT = "CONNECTOR";
  private static final String UPPERCASE_PORTAL_SEARCH_TEXT = "PORT";
  private static final String UNKNOWN_SEARCH_TEXT = "no-such-product";
  private static final String BLANK_SEARCH_TEXT = "";

  @Autowired
  private GithubRepoRepository githubRepoRepository;

  @Test
  void shouldIgnoreBlankSearchText() {
    Page<GithubRepo> repos = findAllByFocusedSorted(true, BLANK_SEARCH_TEXT, ASCENDING);

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactlyInAnyOrder(PORTAL_PRODUCT_ID, CONNECTIVITY_PRODUCT_ID, MICROSOFT_365_PRODUCT_ID);
    assertThat(repos.getTotalElements()).isEqualTo(3);
  }

  @Test
  void shouldReturnOnlyRepositoriesWithoutFocusFlag() {
    Page<GithubRepo> repos = findAllByFocusedSorted(null, null, ASCENDING);

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactlyInAnyOrder(JIRA_CONNECTOR_PRODUCT_ID, SLACK_CONNECTOR_PRODUCT_ID)
        .doesNotContain(ARCHIVED_DEMO_PRODUCT_ID);
  }

  @Test
  void shouldTreatFocusedFalseAsUnfocusedFilter() {
    Page<GithubRepo> repos = findAllByFocusedSorted(false, null, ASCENDING);

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactlyInAnyOrder(JIRA_CONNECTOR_PRODUCT_ID, SLACK_CONNECTOR_PRODUCT_ID);
  }

  @Test
  void shouldMapSelectedColumnsIntoEntity() {
    Page<GithubRepo> repos =
        findAllByFocusedSorted(true, UPPERCASE_PORTAL_SEARCH_TEXT, ASCENDING);

    assertThat(repos.getTotalElements()).isEqualTo(1);
    assertThat(repos.getContent()).singleElement().satisfies(repo -> {
      assertThat(repo.getId()).isEqualTo(PORTAL_REPO_ID);
      assertThat(repo.getName()).isEqualTo(PORTAL_PRODUCT_ID);
      assertThat(repo.getProductId()).isEqualTo(PORTAL_PRODUCT_ID);
      assertThat(repo.getHtmlUrl()).isEqualTo(PORTAL_HTML_URL);
      assertThat(repo.getFocused()).isTrue();
    });
  }

  @Test
  void shouldFilterByProductIdCaseInsensitivelyAndPartially() {
    Page<GithubRepo> repos =
        findAllByFocusedSorted(null, UPPERCASE_CONNECTOR_SEARCH_TEXT, ASCENDING);

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactlyInAnyOrder(JIRA_CONNECTOR_PRODUCT_ID, SLACK_CONNECTOR_PRODUCT_ID);
    assertThat(repos.getTotalElements()).isEqualTo(2);
  }

  @Test
  void shouldReturnEmptyPageWhenSearchTextMatchesNothing() {
    Page<GithubRepo> repos = findAllByFocusedSorted(true, UNKNOWN_SEARCH_TEXT, ASCENDING);

    assertThat(repos.getContent()).isEmpty();
    assertThat(repos.getTotalElements()).isZero();
  }

  @Test
  void shouldSortSuccessBeforeFailureWhenDescending() {
    Page<GithubRepo> repos = findAllByFocusedSorted(true, null, DESCENDING);

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactly(PORTAL_PRODUCT_ID, CONNECTIVITY_PRODUCT_ID, MICROSOFT_365_PRODUCT_ID);
    assertThat(repos.getTotalElements()).isEqualTo(3);
  }

  @Test
  void shouldSortFailureBeforeSuccessWhenAscending() {
    Page<GithubRepo> repos = findAllByFocusedSorted(true, null, ASCENDING);

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactly(CONNECTIVITY_PRODUCT_ID, PORTAL_PRODUCT_ID, MICROSOFT_365_PRODUCT_ID);
  }

  @Test
  void shouldPaginateFocusedRepositories() {
    MonitoringSearchCriteria criteria = MonitoringSearchCriteria.builder()
        .isFocused(true)
        .workFlowType(CI_WORKFLOW_TYPE)
        .sortDirection(DESCENDING)
        .build();

    Page<GithubRepo> repos = githubRepoRepository.findAllByFocusedSorted(criteria, PageRequest.of(1, 2));

    assertThat(repos.getContent())
        .extracting(GithubRepo::getProductId)
        .containsExactly(MICROSOFT_365_PRODUCT_ID);
    assertThat(repos.getTotalElements()).isEqualTo(3);
    assertThat(repos.getTotalPages()).isEqualTo(2);
  }

  private Page<GithubRepo> findAllByFocusedSorted(Boolean isFocused, String searchText,
      String sortDirection) {
    MonitoringSearchCriteria criteria = MonitoringSearchCriteria.builder()
        .isFocused(isFocused)
        .searchText(searchText)
        .workFlowType(CustomGithubRepoRepositoryImplTest.CI_WORKFLOW_TYPE)
        .sortDirection(sortDirection)
        .build();
    return githubRepoRepository.findAllByFocusedSorted(criteria, PageRequest.of(0, 10));
  }
}
