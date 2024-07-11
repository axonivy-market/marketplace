package com.axonivy.market.factory;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import com.axonivy.market.github.model.Meta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Product;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {
  private static final String DUMMY_LOGO_URL =
      "https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/amazon-comprehend-connector/logo.png";

  @Test
  void testMappingByGHContent() throws IOException {
    Product product = new Product();
    GHContent mockContent = mock(GHContent.class);
    var result = ProductFactory.mappingByGHContent(product, null);
    assertEquals(product, result);
    when(mockContent.getName()).thenReturn(META_FILE);
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);
    result = ProductFactory.mappingByGHContent(product, mockContent);
    assertNotEquals(null, result);
    assertEquals("Amazon Comprehend", result.getNames().getEn());
    assertEquals("Amazon Comprehend DE", result.getNames().getDe());
  }

  @Test
  void testMappingLogo() throws IOException {
    Product product = new Product();
    GHContent content = mock(GHContent.class);
    when(content.getName()).thenReturn(CommonConstants.LOGO_FILE);
    var result = ProductFactory.mappingByGHContent(product, content);
    assertNotEquals(null, result);

    when(content.getName()).thenReturn(CommonConstants.LOGO_FILE);
    when(content.getDownloadUrl()).thenReturn(DUMMY_LOGO_URL);
    result = ProductFactory.mappingByGHContent(product, content);
    assertNotEquals(null, result);
  }

  @Test
  void testExtractSourceUrl() throws IOException {
    Product product = new Product();
    Meta meta = new Meta();
    ProductFactory.extractSourceUrl(product, meta);
    Assertions.assertNull(product.getRepositoryName());
    Assertions.assertNull(product.getSourceUrl());

    String sourceUrl = "https://github.com/axonivy-market/alfresco-connector";
    meta.setSourceUrl(sourceUrl);
    ProductFactory.extractSourceUrl(product, meta);
    Assertions.assertEquals("axonivy-market/alfresco-connector", product.getRepositoryName());
    Assertions.assertEquals(sourceUrl, product.getSourceUrl());

    sourceUrl = "portal";
    meta.setSourceUrl(sourceUrl);
    ProductFactory.extractSourceUrl(product, meta);
    Assertions.assertEquals(sourceUrl, product.getRepositoryName());
    Assertions.assertEquals(sourceUrl, product.getSourceUrl());
  }
}
