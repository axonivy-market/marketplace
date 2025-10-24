package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.GithubRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class CustomGithubRepoRepositoryImplTest {

  private EntityManager entityManager;
  private CustomGithubRepoRepositoryImpl customGithubRepoRepository;

  @BeforeEach
  void setUp() {
    entityManager = mock(EntityManager.class);
    customGithubRepoRepository = new CustomGithubRepoRepositoryImpl(entityManager);
    // Use reflection to inject the mock because @PersistenceContext isn't set in test
    try {
      FieldUtils.writeField(customGithubRepoRepository, "entityManager", entityManager, true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testFindAllByFocusedSortedWithFocusedTrueAndProductId() {
    // Arrange
    String workflowType = "build";
    String sortDirection = "DESC";
    String productId = "test-product";
    Boolean isFocused = true;
    Pageable pageable = PageRequest.of(0, 10);

    Query nativeQuery = mock(Query.class);
    Query countQuery = mock(Query.class);

    GithubRepo repo = new GithubRepo();
    List<GithubRepo> repoList = Collections.singletonList(repo);

    // Mock native query and count query
    when(entityManager.createNativeQuery(anyString(), eq(GithubRepo.class))).thenReturn(nativeQuery);
    when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery);

    when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
    when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);

    when(nativeQuery.setFirstResult(anyInt())).thenReturn(nativeQuery);
    when(nativeQuery.setMaxResults(anyInt())).thenReturn(nativeQuery);

    when(nativeQuery.getResultList()).thenReturn(repoList);
    when(countQuery.getSingleResult()).thenReturn(1L);

    // Act
    Page<GithubRepo> result = customGithubRepoRepository.findAllByFocusedSorted(isFocused, workflowType, sortDirection, productId, pageable);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getTotalElements(), "Total elements should be 1");
    assertEquals(repoList, result.getContent(), "Content should match the expected repo list");

    // Verify query construction and parameter setting
    verify(entityManager, times(1)).createNativeQuery(anyString(), eq(GithubRepo.class));
    verify(entityManager, times(1)).createNativeQuery(anyString());
    verify(nativeQuery, times(1)).setParameter("workflowType", workflowType);
    verify(nativeQuery, times(1)).setParameter("productId", productId.toLowerCase());
    verify(countQuery, times(1)).setParameter("productId", productId.toLowerCase());
    verify(nativeQuery, times(1)).setFirstResult(0);
    verify(nativeQuery, times(1)).setMaxResults(10);
    verify(nativeQuery, times(1)).getResultList();
    verify(countQuery, times(1)).getSingleResult();
  }

  @Test
  void testFindAllByFocusedSortedWithFocusedNullAndNoProductId() {
    // Arrange
    String workflowType = "build";
    String sortDirection = "ASC";
    String productId = null;
    Boolean isFocused = null;
    Pageable pageable = PageRequest.of(1, 5); // offset: 5

    Query nativeQuery = mock(Query.class);
    Query countQuery = mock(Query.class);

    GithubRepo repo = new GithubRepo();
    List<GithubRepo> repoList = List.of(repo);

    when(entityManager.createNativeQuery(anyString(), eq(GithubRepo.class))).thenReturn(nativeQuery);
    when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery);

    when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
    when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);

    when(nativeQuery.setFirstResult(anyInt())).thenReturn(nativeQuery);
    when(nativeQuery.setMaxResults(anyInt())).thenReturn(nativeQuery);

    when(nativeQuery.getResultList()).thenReturn(repoList);
    when(countQuery.getSingleResult()).thenReturn(1L);

    // Act
    Page<GithubRepo> result = customGithubRepoRepository.findAllByFocusedSorted(isFocused, workflowType, sortDirection, productId, pageable);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getContent().size(), "Content size should be 1");
    assertEquals(repoList, result.getContent(), "Content should match the expected repo list");

    verify(nativeQuery, times(1)).setParameter("workflowType", workflowType);
    verify(nativeQuery, never()).setParameter(eq("productId"), any());
    verify(countQuery, never()).setParameter(eq("productId"), any());
    verify(nativeQuery, times(1)).setFirstResult(5);
    verify(nativeQuery, times(1)).setMaxResults(5);
  }
}
