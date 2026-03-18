package com.axonivy.market.core.respository.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductModuleContentRepository;
import com.axonivy.market.core.repository.impl.CoreCustomProductRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoreCustomProductRepositoryImplTest extends CoreBaseSetup {

  @Mock
  CoreProductModuleContentRepository coreProductModuleContentRepository;

  @Mock
  private CoreMetadataRepository coreMetadataRepository;

  @InjectMocks
  private CoreCustomProductRepositoryImpl coreCustomProductRepository;

  @Mock
  private EntityManager em;

  private Product mockProduct;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(coreCustomProductRepository, "entityManager", em);
  }

  @Test
  void testGetProductByIdAndVersion() {
    when(coreProductModuleContentRepository.findByVersionAndProductId(anyString(), anyString())).thenReturn(
        getMockProductModuleContent());
    List<String> expectedVersions = List.of("1.0", "1.1");
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);
    mockProduct.setReleasedVersions(expectedVersions);

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);


    Product actualProduct = coreCustomProductRepository.getProductByIdAndVersion(MOCK_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, actualProduct, "Expected the returned product to match the mocked product");
    assertThat(actualProduct.getProductModuleContent())
        .as("Expected product module content to match the mocked content")
        .usingRecursiveComparison()
        .isEqualTo(getMockProductModuleContent());
    verify(coreProductModuleContentRepository).findByVersionAndProductId(anyString(), anyString());
  }

  @Test
  void testGetReleasedVersionsById() {
    List<String> expectedVersions = List.of("1.0", "1.1");
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);
    mockProduct.setReleasedVersions(expectedVersions);

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);

    List<String> actualReleasedVersions = coreCustomProductRepository.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions,
        "Expected released versions returned from repository to match the product’s released versions");
  }

  @Test
  void testReleasedVersionsByIdWhenResultIsNull() {
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);

    List<String> results = coreCustomProductRepository.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(0, results.size(),
        "Expected released versions list to be empty when the product has no released versions");
  }
}
