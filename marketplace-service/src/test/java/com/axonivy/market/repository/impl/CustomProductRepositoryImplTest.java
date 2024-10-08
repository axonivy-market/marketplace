package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductModuleContent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomProductRepositoryImplTest extends BaseSetup {
  private static final String ID = "bmpn-statistic";
  private static final String TAG = "v10.0.21";
  @Mock
  ProductModuleContentRepository contentRepo;
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

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION),
        eq(Product.class))).thenReturn(aggregationResults);
    mockProduct = new Product();
    mockProduct.setId(ID);
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
  void testGetProductById_andFindProductModuleContentByNewestVersion() {
    mockAggregation = mock(Aggregation.class);
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION),
        eq(Product.class))).thenReturn(aggregationResults);

    ProductModuleContent productModuleContent = ProductModuleContent.builder()
        .productId("bmpn-statistic")
        .tag("v11.3.0")
        .build();

    when(contentRepo.findByTagAndProductId("v11.3.0", ID)).thenReturn(productModuleContent);

    mockProduct = Product.builder()
        .id(ID)
        .newestReleaseVersion("12.0.0-m264")
        .releasedVersions(List.of("11.1.1", "11.1.0", "11.3.0"))
        .productModuleContent(productModuleContent)
        .build();

    when(aggregationResults.getUniqueMappedResult()).thenReturn(mockProduct);

    Product actualProduct = repo.getProductByIdWithNewestReleaseVersion(ID,false);

    verify(contentRepo, times(1)).findByTagAndProductId("v11.3.0", ID);
    assertEquals(mockProduct, actualProduct);
  }

  @Test
  void testGetProductByIdAndTag() {
    setUpMockAggregateResult();
    Product actualProduct = repo.getProductByIdWithTagOrVersion(ID, TAG);
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
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(Product.class))).thenReturn(product);
    int updatedCount = repo.increaseInstallationCount(productId);
    assertEquals(5, updatedCount);
    verify(mongoTemplate).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(Product.class));
  }

  @Test
  void testIncreaseInstallationCount_NullProduct() {
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(Product.class))).thenReturn(null);
    int updatedCount = repo.increaseInstallationCount(ID);
    assertEquals(0, updatedCount);
  }

  @Test
  void testUpdateInitialCount() {
    setUpMockAggregateResult();
    int initialCount = 10;
    repo.updateInitialCount(ID, initialCount);
    verify(mongoTemplate).updateFirst(any(Query.class),
        eq(new Update().inc("InstallationCount", initialCount).set("SynchronizedInstallationCount", true)),
        eq(Product.class));
  }

  @Test
  void testIncreaseInstallationCountForProductByDesignerVersion() {
    repo.increaseInstallationCountForProductByDesignerVersion("portal", "10.0.20");
    verify(mongoTemplate).upsert(any(Query.class), any(Update.class), eq(ProductDesignerInstallation.class));
  }
}
