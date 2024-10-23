package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
class ProductJsonContentServiceImplTest extends BaseSetup {
  @Mock
  private ProductJsonContentRepository productJsonRepo;
  @Mock
  private ProductRepository productRepo;

  @InjectMocks
  private ProductJsonContentServiceImpl productJsonContentService;

  @Test
  void testUpdateProductJsonContent_ValidJsonContent() {
    String jsonContent = "{\"version\":\"${version}\"}";
    Product product = new Product();
    product.setId(MOCK_PRODUCT_ID);
    HashMap<String, String> names = new HashMap<>();
    names.put(ProductJsonConstants.EN_LANGUAGE, MOCK_PRODUCT_NAME);
    product.setNames(names);

    Mockito.when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));

    productJsonContentService.updateProductJsonContent(jsonContent, MOCK_RELEASED_VERSION,
        ProductJsonConstants.VERSION_VALUE, MOCK_PRODUCT_ID);

    ArgumentCaptor<ProductJsonContent> captor = ArgumentCaptor.forClass(ProductJsonContent.class);
    Mockito.verify(productJsonRepo).save(captor.capture());

    ProductJsonContent savedContent = captor.getValue();
    assertEquals(MOCK_RELEASED_VERSION, savedContent.getVersion());
    assertEquals(MOCK_PRODUCT_ID, savedContent.getProductId());
    assertEquals(MOCK_PRODUCT_NAME, savedContent.getName());
    assertEquals("{\"version\":\"" + MOCK_RELEASED_VERSION + "\"}", savedContent.getContent());
  }

  @Test
  void testUpdateProductJsonContent_EmptyJsonContent() {
    productJsonContentService.updateProductJsonContent(StringUtils.EMPTY, MOCK_SNAPSHOT_VERSION,
        ProductJsonConstants.VERSION_VALUE, MOCK_PRODUCT_ID);
    Mockito.verify(productJsonRepo, Mockito.never()).save(any(ProductJsonContent.class));
  }
}