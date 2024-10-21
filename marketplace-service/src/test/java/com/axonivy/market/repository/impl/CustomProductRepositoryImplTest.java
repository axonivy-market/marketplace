package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomProductRepositoryImplTest extends BaseSetup {
  @Mock
  ProductModuleContentRepository contentRepo;
  @Mock
  ProductJsonContentRepository jsonContentRepo;
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
  void testGetProductById() {
    setUpMockAggregateResult();
    Product actualProduct = repo.getProductById(MOCK_PRODUCT_ID);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testGetProductByIdAndTag() {
    setUpMockAggregateResult();
    Product actualProduct = repo.getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_TAG_FROM_RELEASED_VERSION);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testGetReleasedVersionsById() {
    setUpMockAggregateResult();
    List<String> actualReleasedVersions = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions);
  }

  @Test
  void testIncreaseInstallationCount() {
    Product product = new Product();
    product.setId(MOCK_PRODUCT_ID);
    product.setInstallationCount(5);
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(Product.class))).thenReturn(product);
    int updatedCount = repo.increaseInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(5, updatedCount);
    verify(mongoTemplate).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(Product.class));
  }

  @Test
  void testIncreaseInstallationCount_NullProduct() {
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(Product.class))).thenReturn(null);
    int updatedCount = repo.increaseInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(0, updatedCount);
  }

  @Test
  void testUpdateInitialCount() {
    setUpMockAggregateResult();
    int initialCount = 10;
    repo.updateInitialCount(MOCK_PRODUCT_ID, initialCount);
    verify(mongoTemplate).updateFirst(any(Query.class),
        eq(new Update().inc(MongoDBConstants.INSTALLATION_COUNT, initialCount).set(MongoDBConstants.SYNCHRONIZED_INSTALLATION_COUNT, true)),
        eq(Product.class));
  }

  @Test
  void testIncreaseInstallationCountForProductByDesignerVersion() {
    repo.increaseInstallationCountForProductByDesignerVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    verify(mongoTemplate).upsert(any(Query.class), any(Update.class), eq(ProductDesignerInstallation.class));
  }
}
