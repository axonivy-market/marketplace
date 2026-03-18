package com.axonivy.market.core.respository.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.ProductDesignerInstallation;
import com.axonivy.market.core.repository.impl.CoreCustomProductDesignerInstallationRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class CoreCustomProductDesignerInstallationRepositoryImplTest extends CoreBaseSetup {
  @Mock
  private EntityManager em;

  @InjectMocks
  private CoreCustomProductDesignerInstallationRepositoryImpl coreCustomProductDesignerInstallationRepositoryImpl;

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

    coreCustomProductDesignerInstallationRepositoryImpl.increaseInstallationCountForProductByDesignerVersion(
        MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    verify(query).executeUpdate();
    verify(em).createNativeQuery(anyString());
  }

  @Test
  void testIncreaseInstallationCountWhenNoExistingInstallationCreatesNew() {
    var repo = Mockito.spy(new TestableCoreCustomProductDesignerInstallationRepositoryImpl());

    TypedQuery<ProductDesignerInstallation> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<ProductDesignerInstallation> cq = mock(CriteriaQuery.class);
    Root<ProductDesignerInstallation> root = mock(Root.class);

    ReflectionTestUtils.setField(repo, "entityManager", em);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(ProductDesignerInstallation.class)).thenReturn(cq);
    when(cq.from(ProductDesignerInstallation.class)).thenReturn(root);
    when(em.createQuery(cq)).thenReturn(query);
    when(query.getResultList()).thenReturn(Collections.emptyList());

    repo.increaseInstallationCountForProductByDesignerVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    ProductDesignerInstallation saved = repo.getCaptured();
    assertNotNull(saved, "A new ProductDesignerInstallation should be saved");
    assertEquals(MOCK_PRODUCT_ID, saved.getProductId(), "ProductId must be set correctly");
    assertEquals(MOCK_RELEASED_VERSION, saved.getDesignerVersion(), "DesignerVersion must be set correctly");
    assertEquals(1, saved.getInstallationCount(), "InstallationCount must start at 1");
  }
}
