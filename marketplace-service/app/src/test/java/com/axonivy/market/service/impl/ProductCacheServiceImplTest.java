package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceImplTest extends BaseSetup {

  private static final String UNKNOWN_PRODUCT = "unknown-product";

  @Mock
  private ProductRepository productRepo;

  @Mock
  private AppSettingService appSettingService;

  @InjectMocks
  private ProductCacheServiceImpl productCacheService;

  @Test
  void testIsValidProductIdReturnsFalseForNull() {
    assertFalse(productCacheService.isValidProductId(null));
  }

  @Test
  void testIsValidProductIdAndVersionReturnsFalseForNullArguments() {
    assertFalse(productCacheService.isValidProductIdAndVersion(null, MOCK_RELEASED_VERSION));
    assertFalse(productCacheService.isValidProductIdAndVersion(MOCK_PRODUCT_ID, null));
  }

  @Test
  void testIsValidProductIdLoadsCacheFromDatabaseOnce() {
    when(appSettingService.getLongValueByKey(AppSettingKey.PRODUCT_ID_CACHE_EXPIRATION_MINUTES)).thenReturn(60L);
    Product product = buildProduct(MOCK_PRODUCT_ID, List.of(MOCK_RELEASED_VERSION));
    when(productRepo.findAll()).thenReturn(List.of(product));

    assertTrue(productCacheService.isValidProductId(MOCK_PRODUCT_ID));
    assertFalse(productCacheService.isValidProductId(UNKNOWN_PRODUCT));

    // The cache must be filled once in bulk from Product entities, never re-queried using the caller-supplied id.
    verify(productRepo, times(1)).findAll();
  }

  @Test
  void testIsValidProductIdAndVersionMatchesCachedCombination() {
    when(appSettingService.getLongValueByKey(AppSettingKey.PRODUCT_ID_CACHE_EXPIRATION_MINUTES)).thenReturn(60L);
    Product product = buildProduct(MOCK_PRODUCT_ID, List.of(MOCK_RELEASED_VERSION));
    when(productRepo.findAll()).thenReturn(List.of(product));

    assertTrue(productCacheService.isValidProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION));
    assertFalse(productCacheService.isValidProductIdAndVersion(MOCK_PRODUCT_ID, "unknown-version"));
    assertFalse(productCacheService.isValidProductIdAndVersion(UNKNOWN_PRODUCT, MOCK_RELEASED_VERSION));
  }

  private Product buildProduct(String productId, List<String> releasedVersions) {
    Product product = new Product();
    product.setId(productId);
    product.setReleasedVersions(releasedVersions);
    return product;
  }
}
