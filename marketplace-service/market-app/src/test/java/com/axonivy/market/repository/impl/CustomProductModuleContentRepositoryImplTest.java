package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ProductModuleContent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.axonivy.market.constants.PostgresDBConstants.PRODUCT_ID;
import static com.axonivy.market.constants.PostgresDBConstants.VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CustomProductModuleContentRepositoryImplTest extends BaseSetup {
  @Mock
  private EntityManager em;

  @InjectMocks
  CustomProductModuleContentRepositoryImpl repository;

  @Test
  void testFindVersionsByProductId() {
    TypedQuery<String> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<String> cq = mock(CriteriaQuery.class);
    Root<ProductModuleContent> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(String.class)).thenReturn(cq);
    when(cq.from(ProductModuleContent.class)).thenReturn(productRoot);
    when(em.createQuery(cq)).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of("1"));

    var mocKPath = mock(Path.class);

    when(cq.select(mocKPath)).thenReturn(cq);

    when(productRoot.get(VERSION)).thenReturn(mocKPath);
    when(productRoot.get(PRODUCT_ID)).thenReturn(mocKPath);
    Predicate predicate = mock(Predicate.class);
    when(cb.equal(any(),anyString())).thenReturn(predicate);

    List<String> result = repository.findVersionsByProductId(MOCK_PRODUCT_ID);

    assertNotNull(result, "Expected result list to be non-null");
    assertEquals(1, result.size(), "Expected result list to contain exactly 1 version");
  }
}
