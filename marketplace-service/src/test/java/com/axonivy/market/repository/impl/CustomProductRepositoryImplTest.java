package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomProductRepositoryImplTest extends BaseSetup {
  @Mock
  ProductModuleContentRepository contentRepo;
  private Product mockProduct;
  private Aggregation mockAggregation;
  @Mock
  private MongoTemplate mongoTemplate;
  @Mock
  private MetadataRepository metadataRepo;
  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepo;
  @InjectMocks
  private CustomProductRepositoryImpl repo;

  @Mock
  private EntityManager em;

  private void setUpMockAggregateResult() {
    mockAggregation = mock(Aggregation.class);
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION),
        eq(Product.class))).thenReturn(aggregationResults);
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(mockProduct);
  }



  @Test
  void testGetProductByIdAndVersion() {
//    setUpMockAggregateResult();
//    Product actualProduct = repo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
//    assertEquals(mockProduct, actualProduct);

//    setUpMockAggregateResult();

    when(contentRepo.findByVersionAndProductId(anyString(),anyString())).thenReturn(getMockProductModuleContent());
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


    Product actualProduct = repo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, actualProduct);
    assertThat(actualProduct.getProductModuleContent()).usingRecursiveComparison().isEqualTo(getMockProductModuleContent());
    verify(contentRepo, times(1)).findByVersionAndProductId(anyString(),anyString());
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

    List<String> actualReleasedVersions = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions);
  }

  @Test
  void testReleasedVersionsById_WhenResultIsNull() {
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

    List<String> results = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(0, results.size());
  }
}
