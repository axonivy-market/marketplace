package com.axonivy.market.repository.impl;

import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.ProductSecuritySortOption;
import com.axonivy.market.repository.ProductSecurityInfoRepository;
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
class CustomProductSecurityInfoRepositoryImplTest {
  private static final String SEARCH_TEXT = "portal";
  private static final String ALPHA_SECURITY = "alpha-security";
  private static final String PORTAL_CONNECTOR = "portal-connector";
  private static final String ZETA_SECURITY = "zeta-security";

  @Autowired
  private ProductSecurityInfoRepository repository;

  @Test
  void shouldReturnAllRepositoriesWhenSearchTextIsBlank() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent()).extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(ALPHA_SECURITY, PORTAL_CONNECTOR, ZETA_SECURITY);
  }

  @Test
  void shouldFilterRepositoriesBySearchText() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .searchText(SEARCH_TEXT)
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).singleElement()
        .extracting(ProductSecurityInfo::getRepoName)
        .isEqualTo(PORTAL_CONNECTOR);
  }

  @Test
  void shouldTrimSearchTextBeforeSearching() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .searchText("  portal  ")
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(PORTAL_CONNECTOR);
  }

  @Test
  void shouldSortByRepositoryNameDescending() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("DESC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getContent()).extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(ZETA_SECURITY, PORTAL_CONNECTOR, ALPHA_SECURITY);
  }

  @Test
  void shouldSortByBranchProtection() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.BRANCH_PROTECTION)
        .sortDirection("DESC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getContent()).extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(ALPHA_SECURITY, ZETA_SECURITY, PORTAL_CONNECTOR);
  }

  @Test
  void shouldSortByCommitDate() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.COMMIT_DATE)
        .sortDirection("DESC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getContent()).extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(PORTAL_CONNECTOR, ZETA_SECURITY, ALPHA_SECURITY);
  }

  @Test
  void shouldApplyPagination() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(1, 1));

    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getTotalPages()).isEqualTo(3);
    assertThat(result.getContent()).extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(PORTAL_CONNECTOR);
  }
}
