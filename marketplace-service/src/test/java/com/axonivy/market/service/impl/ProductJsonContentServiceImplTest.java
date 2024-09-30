package com.axonivy.market.service.impl;

import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.repository.ProductJsonContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductJsonContentServiceImplTest {
  @Mock
  private ProductJsonContentRepository productJsonRepo;

  @InjectMocks
  private ProductJsonContentServiceImpl productJsonContentService;

  @Test
  void testUpdateProductJsonContent_ValidJsonContent() {
    String jsonContent = "{\"version\":\"${version}\"}";
    String currentVersion = "1.0.0";
    Product product = new Product();
    product.setId("123");
    HashMap<String, String> names = new HashMap<>();
    names.put(ProductJsonConstants.EN_LANGUAGE, "Test Product");
    product.setNames(names);

    productJsonContentService.updateProductJsonContent(jsonContent, currentVersion,
        ProductJsonConstants.VERSION_VALUE, product);

    ArgumentCaptor<ProductJsonContent> captor = ArgumentCaptor.forClass(ProductJsonContent.class);
    verify(productJsonRepo, times(1)).save(captor.capture());

    ProductJsonContent savedContent = captor.getValue();
    assertEquals(currentVersion, savedContent.getVersion());
    assertEquals("123", savedContent.getProductId());
    assertEquals("Test Product", savedContent.getName());
    assertEquals("{\"version\":\"1.0.0\"}", savedContent.getContent());
  }

  @Test
  void testUpdateProductJsonContent_EmptyJsonContent() {
    String jsonContent = "";
    String currentVersion = "1.0.0";
    Product product = new Product();

    productJsonContentService.updateProductJsonContent(jsonContent, currentVersion,
        ProductJsonConstants.VERSION_VALUE, product);

    verify(productJsonRepo, times(0)).save(any(ProductJsonContent.class));
  }
}