package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    Product actualProduct = repo.getProductByIdAndTag(ID, TAG);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testCreateDocumentFilterProductModuleContentByTag() {
    Document expectedCondition = new Document(MongoDBConstants.EQUAL,
        Arrays.asList(MongoDBConstants.PRODUCT_MODULE_CONTENT_TAG, TAG));
    Document expectedLoop = new Document(MongoDBConstants.INPUT, MongoDBConstants.PRODUCT_MODULE_CONTENT_QUERY)
        .append(MongoDBConstants.AS, MongoDBConstants.PRODUCT_MODULE_CONTENT)
        .append(MongoDBConstants.CONDITION, expectedCondition);

    Document result = repo.createDocumentFilterProductModuleContentByTag(TAG);

    assertEquals(expectedLoop, result, "The created Document does not match the expected structure.");
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

  @Test
  void testUpdateInitialCount() {
    setUpMockAggregateResult();
    int initialCount = 10;
    repo.updateInitialCount(ID, initialCount);
    verify(mongoTemplate).updateFirst(any(Query.class), eq(new Update().inc("InstallationCount", initialCount).set("SynchronizedInstallationCount", true)), eq(Product.class));
  }

  @Test
  void testIncreaseInstallationCountForProductByDesignerVersion() {
    String productId = "portal";
    String designerVersion = "11.4.0";
    ProductDesignerInstallation productDesignerInstallation = new ProductDesignerInstallation();
    productDesignerInstallation.setProductId(productId);
    productDesignerInstallation.setDesignerVersion(designerVersion);
    productDesignerInstallation.setInstallationCount(5);
    when(mongoTemplate.upsert(any(Query.class), any(Update.class), eq(ProductDesignerInstallation.class))).
            thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    boolean success = repo.increaseInstallationCountForProductByDesignerVersion(productId, designerVersion);
    assertTrue(success);
  }
}
