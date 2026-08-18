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
  void testReturnAllRepositoriesWhenSearchTextIsBlank() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getTotalElements())
        .as("blank search should return all repositories")
        .isEqualTo(3);
    assertThat(result.getContent())
        .as("repositories should be sorted by name ascending")
        .extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(ALPHA_SECURITY, PORTAL_CONNECTOR, ZETA_SECURITY);
  }

  @Test
  void testFilterRepositoriesBySearchText() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .searchText(SEARCH_TEXT)
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getTotalElements())
        .as("search text should narrow the result set")
        .isEqualTo(1);
    assertThat(result.getContent())
        .as("filtered result should contain exactly one repository")
        .singleElement()
        .extracting(ProductSecurityInfo::getRepoName)
        .isEqualTo(PORTAL_CONNECTOR);
  }

  @Test
  void testTrimSearchTextBeforeSearching() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .searchText("  portal  ")
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getTotalElements())
        .as("search text should be trimmed before lookup")
        .isEqualTo(1);
    assertThat(result.getContent())
        .as("trimmed search should still return the matching repository")
        .extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(PORTAL_CONNECTOR);
  }

  @Test
  void testSortByRepositoryNameDescending() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("DESC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getContent())
        .as("repositories should be sorted by name descending")
        .extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(ZETA_SECURITY, PORTAL_CONNECTOR, ALPHA_SECURITY);
  }

  @Test
  void testSortByBranchProtection() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.BRANCH_PROTECTION)
        .sortDirection("DESC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getContent())
        .as("repositories should be sorted by branch protection")
        .extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(ALPHA_SECURITY, ZETA_SECURITY, PORTAL_CONNECTOR);
  }

  @Test
  void testSortByCommitDate() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.COMMIT_DATE)
        .sortDirection("DESC")
        .build(), PageRequest.of(0, 10));

    assertThat(result.getContent())
        .as("repositories should be sorted by commit date")
        .extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(PORTAL_CONNECTOR, ZETA_SECURITY, ALPHA_SECURITY);
  }

  @Test
  void testApplyPagination() {
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build(), PageRequest.of(1, 1));

    assertThat(result.getTotalElements())
        .as("pagination should report the total number of repositories")
        .isEqualTo(3);
    assertThat(result.getTotalPages())
        .as("pagination should report the expected number of pages")
        .isEqualTo(3);
    assertThat(result.getContent())
        .as("second page should contain the middle repository")
        .extracting(ProductSecurityInfo::getRepoName)
        .containsExactly(PORTAL_CONNECTOR);
  }
}
