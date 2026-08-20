package com.axonivy.market.service.impl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
import com.axonivy.market.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceImplTest {

  private static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  private static final String MOCK_VERSION = "10.0.10";

  @Mock
  private ProductRepository productRepo;

  @Mock
  private AppSettingService appSettingService;

  private ProductCacheServiceImpl productCacheService;

  @BeforeEach
  void setUp() {
    productCacheService = new ProductCacheServiceImpl(productRepo, appSettingService);
  }

  @Test
  void testIsValidProductIdReturnsFalseForNull() {
    assertFalse(productCacheService.isValidProductId(null));
  }

  @Test
  void testIsValidProductIdAndVersionReturnsFalseForNullArguments() {
    assertFalse(productCacheService.isValidProductIdAndVersion(null, MOCK_VERSION));
    assertFalse(productCacheService.isValidProductIdAndVersion(MOCK_PRODUCT_ID, null));
  }

  @Test
  void testIsValidProductIdLoadsCacheFromDatabaseOnce() {
    when(appSettingService.getLongValueByKey(AppSettingKey.PRODUCT_CACHE_EXPIRATION_MINUTES)).thenReturn(60L);
    Product product = buildProduct(MOCK_PRODUCT_ID, List.of(MOCK_VERSION));
    when(productRepo.findAll()).thenReturn(List.of(product));

    assertTrue(productCacheService.isValidProductId(MOCK_PRODUCT_ID));
    assertFalse(productCacheService.isValidProductId("unknown-product"));

    // The cache must be filled once in bulk from Product entities, never re-queried using the caller-supplied id.
    verify(productRepo, times(1)).findAll();
  }

  @Test
  void testIsValidProductIdAndVersionMatchesCachedCombination() {
    when(appSettingService.getLongValueByKey(AppSettingKey.PRODUCT_CACHE_EXPIRATION_MINUTES)).thenReturn(60L);
    Product product = buildProduct(MOCK_PRODUCT_ID, List.of(MOCK_VERSION));
    when(productRepo.findAll()).thenReturn(List.of(product));

    assertTrue(productCacheService.isValidProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_VERSION));
    assertFalse(productCacheService.isValidProductIdAndVersion(MOCK_PRODUCT_ID, "unknown-version"));
    assertFalse(productCacheService.isValidProductIdAndVersion("unknown-product", MOCK_VERSION));
  }

  private Product buildProduct(String productId, List<String> releasedVersions) {
    Product product = new Product();
    product.setId(productId);
    product.setReleasedVersions(releasedVersions);
    return product;
  }
}
