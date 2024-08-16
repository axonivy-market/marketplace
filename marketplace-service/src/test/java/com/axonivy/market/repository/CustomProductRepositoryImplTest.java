package com.axonivy.market.repository;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.impl.CustomProductRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

  @BeforeEach
  void setup() {

  }

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

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION), eq(Product.class)))
        .thenReturn(aggregationResults);
    mockProduct = new Product();
    mockProduct.setId(ID);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(mockProduct);
  }

  @Test
  void testQueryProductByAggregation_WhenResultIsNull() {
    // Arrange
    Aggregation aggregation = mock(Aggregation.class);
    AggregationResults<Product> aggregationResults = mock(AggregationResults.class);

    when(mongoTemplate.aggregate(any(Aggregation.class), eq(MongoDBConstants.PRODUCT_COLLECTION), eq(Product.class)))
        .thenReturn(aggregationResults);
    when(aggregationResults.getUniqueMappedResult()).thenReturn(null);

    // Act
    Product actualProduct = repo.queryProductByAggregation(aggregation);

    // Assert
    assertNull(actualProduct);
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
  void testGetReleasedVersionsById() {
    setUpMockAggregateResult();
    List<String> actualReleasedVersions = repo.getReleasedVersionsById(ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions);
  }
}
