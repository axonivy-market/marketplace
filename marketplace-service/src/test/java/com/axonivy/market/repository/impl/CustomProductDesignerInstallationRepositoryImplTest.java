package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ProductDesignerInstallation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomProductDesignerInstallationRepositoryImplTest extends BaseSetup {
  @Mock
  private EntityManager em;

  @InjectMocks
  private CustomProductDesignerInstallationRepositoryImpl repository;

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

    repository.increaseInstallationCountForProductByDesignerVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    verify(query).executeUpdate();
    verify(em).createNativeQuery(anyString());
  }
}
