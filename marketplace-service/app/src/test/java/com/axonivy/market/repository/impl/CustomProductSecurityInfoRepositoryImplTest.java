package com.axonivy.market.repository.impl;

import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.ProductSecuritySortOption;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomProductSecurityInfoRepositoryImplTest {

  @Mock
  private EntityManager entityManager;

  private CustomProductSecurityInfoRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    repository = new CustomProductSecurityInfoRepositoryImpl(entityManager);
  }

  @Test
  void testSearchProductSecurityWithNoSearchTextShouldNotBindParameter() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    ProductSecurityInfo info = new ProductSecurityInfo();
    info.setRepoName("repo-a");

    Query dataQuery = mockQueryExecution(List.of(info));
    Query countQuery = mockCountQuery(1L);
    mockCreateNativeQuery(dataQuery, countQuery);

    // Act
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    assertNotNull(result, "Expected non-null result page");
    assertEquals(1, result.getContent().size(), "Expected one repository in result");
    assertEquals("repo-a", result.getContent().getFirst().getRepoName(),
        "Expected repo name to match mocked value");
    verify(dataQuery, never()).setParameter(eq(1), anyString());
    verify(countQuery, never()).setParameter(eq(1), anyString());
  }

  @Test
  void testSearchProductSecurityWithEmptySearchTextShouldNotBindParameter() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .searchText("   ")
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("DESC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    Query dataQuery = mockQueryExecution(List.of());
    Query countQuery = mockCountQuery(0L);
    mockCreateNativeQuery(dataQuery, countQuery);

    // Act
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    assertNotNull(result, "Expected non-null result even when search text is blank");
    assertTrue(result.isEmpty(), "Expected empty page when no repos exist");
    verify(dataQuery, never()).setParameter(eq(1), anyString());
  }

  @Test
  void testSearchProductSecurityWithSearchTextShouldBindLikeParameter() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .searchText("portal")
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    ProductSecurityInfo info = new ProductSecurityInfo();
    info.setRepoName("portal-connector");

    Query dataQuery = mockQueryExecution(List.of(info));
    Query countQuery = mockCountQuery(1L);
    mockCreateNativeQuery(dataQuery, countQuery);

    // Act
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    assertNotNull(result, "Expected non-null result page when search text is provided");
    assertEquals(1, result.getContent().size(), "Expected one matching repository");
    verify(dataQuery).setParameter(1, "%portal%");
    verify(countQuery).setParameter(1, "%portal%");
  }

  @Test
  void testSearchProductSecurityWithSearchTextWithSpacesShouldTrimBeforeBinding() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .searchText("  portal  ")
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    Query dataQuery = mockQueryExecution(List.of());
    Query countQuery = mockCountQuery(0L);
    mockCreateNativeQuery(dataQuery, countQuery);

    // Act
    repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert: parameter must be trimmed before wrapping with %
    verify(dataQuery).setParameter(1, "%portal%");
    verify(countQuery).setParameter(1, "%portal%");
  }

  @Test
  void testSearchProductSecurityPaginationOffsetAndLimitAreApplied() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build();
    Pageable pageable = PageRequest.of(2, 5); // offset = 10, page size = 5

    Query dataQuery = mockQueryExecution(List.of());
    Query countQuery = mockCountQuery(30L);
    mockCreateNativeQuery(dataQuery, countQuery);

    // Act
    Page<ProductSecurityInfo> result = repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    assertEquals(30L, result.getTotalElements(), "Expected total elements from count query");
    verify(dataQuery).setFirstResult(10);
    verify(dataQuery).setMaxResults(5);
  }

  @Test
  void testSearchProductSecurityWithSearchTextSqlShouldContainWhereClause() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .searchText("ivy")
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    Query dataQuery = mockQueryExecution(List.of());
    Query countQuery = mockCountQuery(0L);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    when(entityManager.createNativeQuery(sqlCaptor.capture(), eq(ProductSecurityInfo.class))).thenReturn(dataQuery);
    when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery);

    // Act
    repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    String capturedSql = sqlCaptor.getValue();
    assertTrue(capturedSql.contains("WHERE psi.repo_name ILIKE"),
        "Expected WHERE clause with ILIKE in SQL when search text is provided");
  }

  @Test
  void testSearchProductSecurityWithNoSearchTextSqlShouldNotContainWhereClause() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.REPO_NAME)
        .sortDirection("ASC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    Query dataQuery = mockQueryExecution(List.of());
    Query countQuery = mockCountQuery(0L);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    when(entityManager.createNativeQuery(sqlCaptor.capture(), eq(ProductSecurityInfo.class))).thenReturn(dataQuery);
    when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery);

    // Act
    repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    String capturedSql = sqlCaptor.getValue();
    assertFalse(capturedSql.contains("WHERE"),
        "Expected no WHERE clause in SQL when search text is not provided");
  }

  @Test
  void testSearchProductSecurityWithDependabotAlertSortSqlShouldContainJsonbCast() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .sortOption(ProductSecuritySortOption.DEPENDABOT_ALERTS)
        .sortDirection("DESC")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    Query dataQuery = mockQueryExecution(List.of());
    Query countQuery = mockCountQuery(0L);
    when(entityManager.createNativeQuery(sqlCaptor.capture(), eq(ProductSecurityInfo.class))).thenReturn(dataQuery);
    when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery);

    // Act
    repository.searchProductSecurityAndSorting(criteria, pageable);

    // Assert
    String capturedSql = sqlCaptor.getValue();
    assertTrue(capturedSql.contains("dependabot_alerts"),
        "Expected SQL to contain 'dependabot_alerts' when sorting by DEPENDABOT_ALERTS");
    assertTrue(capturedSql.contains("jsonb"),
        "Expected SQL to use jsonb cast when sorting by alert severity");
    assertTrue(capturedSql.contains("critical"),
        "Expected SQL to contain 'critical' severity in ORDER BY clause");
  }

  private Query mockQueryExecution(List<?> results) {
    Query query = mock(Query.class);
    when(query.setFirstResult(any(Integer.class))).thenReturn(query);
    when(query.setMaxResults(any(Integer.class))).thenReturn(query);
    when(query.getResultList()).thenReturn(results);
    return query;
  }

  private Query mockCountQuery(long count) {
    Query query = mock(Query.class);
    when(query.getSingleResult()).thenReturn(count);
    return query;
  }

  private void mockCreateNativeQuery(Query dataQuery, Query countQuery) {
    when(entityManager.createNativeQuery(anyString(), eq(ProductSecurityInfo.class))).thenReturn(dataQuery);
    when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery);
  }
}

