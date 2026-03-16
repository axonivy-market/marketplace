package com.axonivy.market.core.respository.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.impl.CoreCustomProductMarketplaceDataRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoreCustomProductMarketplaceDataRepositoryImplTest extends CoreBaseSetup {
  @Mock
  private EntityManager em;

  @InjectMocks
  private CoreCustomProductMarketplaceDataRepositoryImpl coreCustomProductMarketplaceDataRepositoryImpl;

  @Test
  void testIncreaseInstallationCount() {
    Query query = mock(Query.class);
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getSingleResult()).thenReturn(getMockProductMarketplaceData().getInstallationCount());

    int updatedCount = coreCustomProductMarketplaceDataRepositoryImpl.increaseInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(3, updatedCount, "Expected installation count to be incremented to 3");
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

    int updatedCount = coreCustomProductMarketplaceDataRepositoryImpl.updateInitialCount(MOCK_PRODUCT_ID, initialCount);
    assertEquals(11, updatedCount, "Expected installation count to be updated from 10 to 11");
  }
}
