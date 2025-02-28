package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductMarketplaceData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomProductMarketplaceDataRepositoryImplTest extends BaseSetup {
  @Mock
  private MongoTemplate mongoTemplate;

  @Mock
  private EntityManager em;

  @InjectMocks
  private CustomProductMarketplaceDataRepositoryImpl repo;

  @Test
  void testIncreaseInstallationCount() {
    Query query = mock(Query.class);
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getSingleResult()).thenReturn(getMockProductMarketplaceData().getInstallationCount());

    int updatedCount = repo.increaseInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(3, updatedCount);
  }

  @Test
  void testUpdateInitialCount() {
    int initialCount = 10;
    Query query = mock(Query.class);
    CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
    CriteriaUpdate<ProductMarketplaceData> mockCriteriaUpdate = mock(CriteriaUpdate.class);
    Root<ProductMarketplaceData> root = mock(Root.class);

    ProductMarketplaceData updatedProductMarketplaceData = new ProductMarketplaceData();
    updatedProductMarketplaceData.setId(MOCK_PRODUCT_ID);
    updatedProductMarketplaceData.setInstallationCount(11);

    when(em.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
    when(mockCriteriaBuilder.createCriteriaUpdate(ProductMarketplaceData.class)).thenReturn(mockCriteriaUpdate);
    when(mockCriteriaUpdate.from(ProductMarketplaceData.class)).thenReturn(root);

    when(em.createQuery(mockCriteriaUpdate)).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);
    when(em.find(ProductMarketplaceData.class, MOCK_PRODUCT_ID)).thenReturn(updatedProductMarketplaceData);

    int updatedCount = repo.updateInitialCount(MOCK_PRODUCT_ID, initialCount);
    assertEquals(11, updatedCount);
  }

  @Test
  void testIncreaseInstallationCountForProductByDesignerVersion() {
    TypedQuery query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<ProductDesignerInstallation> cq = mock(CriteriaQuery.class);
    Root<ProductDesignerInstallation> root = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(ProductDesignerInstallation.class)).thenReturn(cq);
    when(cq.from(ProductDesignerInstallation.class)).thenReturn(root);

    when(em.createQuery(cq)).thenReturn(query);
    when(query.getResultList()).thenReturn(createProductDesignerInstallationsMock());

    when(em.createNativeQuery(anyString())).thenReturn(query);

    repo.increaseInstallationCountForProductByDesignerVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    verify(query).executeUpdate();
    verify(em).createNativeQuery(anyString());
  }

  @Test
  void testCheckAndInitProductMarketplaceDataIfNotExist() {
    // Mock dependencies
    TypedQuery<Long> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Long> cq = mock(CriteriaQuery.class);
    Root<ProductMarketplaceData> root = mock(Root.class);
    Predicate predicate = mock(Predicate.class);
    Expression<Long> countExpression = mock(Expression.class);

    // Stubbing method calls
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Long.class)).thenReturn(cq);
    when(cq.from(ProductMarketplaceData.class)).thenReturn(root);

    when(cb.count(root)).thenReturn(countExpression);
    when(cb.equal(root.get("id"), MOCK_PRODUCT_ID)).thenReturn(predicate);

    // Ensure where() and select() return the same query object
    when(cq.select(countExpression)).thenReturn(cq);
    when(cq.where(predicate)).thenReturn(cq);

    when(em.createQuery(cq)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(1L, 0L);

    // Execute the method
    repo.checkAndInitProductMarketplaceDataIfNotExist(MOCK_PRODUCT_ID);

    // Verify interactions
    verify(em, never()).persist(any(ProductMarketplaceData.class));

    // Execute the method
    repo.checkAndInitProductMarketplaceDataIfNotExist(MOCK_PRODUCT_ID);

    verify(em).persist(any(ProductMarketplaceData.class));
  }

}