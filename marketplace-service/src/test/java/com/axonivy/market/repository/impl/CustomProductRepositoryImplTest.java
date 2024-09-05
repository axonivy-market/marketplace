package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class CustomProductRepositoryImplTest extends BaseSetup {
  private static final String ID = "bmpn-statistic";
  private static final String TAG = "v10.0.21";
  private Product mockProduct;
  private Aggregation mockAggregation;

  @Mock
  ProductModuleContentRepository contentRepo;

  @Mock
  private MongoTemplate mongoTemplate;

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

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION), eq(Product.class))).thenReturn(aggregationResults);
    mockProduct = new Product();
    mockProduct.setId(ID);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(mockProduct);
  }

  @Test
  void testQueryProductByAggregation_WhenResultIsNull() {
    Aggregation aggregation = mock(Aggregation.class);
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);
    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION), eq(Product.class))).thenReturn(aggregationResults);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

    Product actualProduct = repo.queryProductByAggregation(aggregation);

    assertNull(actualProduct);
  }

  @Test
  void testReleasedVersionsById_WhenResultIsNull() {
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION), eq(Product.class))).thenReturn(aggregationResults);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

    List<String> results = repo.getReleasedVersionsById(ID);
    assertEquals(0, results.size());
  }

  @Test
  void testGetProductById() {
    setUpMockAggregateResult();
    Product actualProduct = repo.getProductById(ID);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testGetProductByIdAndTag() {
    setUpMockAggregateResult();
    when(contentRepo.findByTagAndProductId(TAG, ID)).thenReturn(null);
    Product actualProduct = repo.getProductByIdAndTag(ID, TAG);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testGetReleasedVersionsById() {
    setUpMockAggregateResult();
    List<String> actualReleasedVersions = repo.getReleasedVersionsById(ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions);
  }

  @Test
  void testIncreaseInstallationCount() {
    String productId = "testProductId";
    Product product = new Product();
    product.setId(productId);
    product.setInstallationCount(5);
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Product.class))).thenReturn(product);
    int updatedCount = repo.increaseInstallationCount(productId);
    assertEquals(5, updatedCount);
    verify(mongoTemplate).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Product.class));
  }

  @Test
  void testIncreaseInstallationCount_NullProduct() {
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Product.class))).thenReturn(null);
    int updatedCount = repo.increaseInstallationCount(ID);
    assertEquals(0, updatedCount);
  }
}
