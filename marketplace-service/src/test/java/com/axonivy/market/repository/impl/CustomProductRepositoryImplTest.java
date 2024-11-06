package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

  @Test
  void testQueryProductByAggregation_WhenResultIsPresent() {
    setUpMockAggregateResult();
    Product actualProduct = repo.queryProductByAggregation(mockAggregation);
    assertNotNull(actualProduct);
    assertEquals(mockProduct, actualProduct);
  }

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
  void testQueryProductByAggregation_WhenResultIsNull() {
    Aggregation aggregation = mock(Aggregation.class);
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);
    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION),
        eq(Product.class))).thenReturn(aggregationResults);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

    Product actualProduct = repo.queryProductByAggregation(aggregation);

    assertNull(actualProduct);
  }

  @Test
  void testReleasedVersionsById_WhenResultIsNull() {
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION),
        eq(Product.class))).thenReturn(aggregationResults);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

    List<String> results = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(0, results.size());
  }

  @Test
  void testGetProductByIdAndVersion() {
    setUpMockAggregateResult();
    Product actualProduct = repo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testGetReleasedVersionsById() {
    setUpMockAggregateResult();
    List<String> actualReleasedVersions = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions);
  }
}
