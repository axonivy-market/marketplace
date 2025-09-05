package com.axonivy.market.factory;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.Language;
import com.axonivy.market.github.model.Meta;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest extends BaseSetup {
  @Test
  void testMappingByGHContent() throws IOException {
    Product product = new Product();
    GHContent mockContent = mock(GHContent.class);
    var result = ProductFactory.mappingByGHContent(product, null);

    assertEquals(product, result, "Mapping with null GHContent should return the original product.");

    when(mockContent.getName()).thenReturn(META_FILE);
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);
    result = ProductFactory.mappingByGHContent(product, mockContent);
    assertNotNull(result, "Mapping with valid GHContent should not return null.");
    assertEquals("Amazon Comprehend", result.getNames().get(Language.EN.getValue()),
        "The English name should be correctly mapped from the GHContent.");
    assertEquals("Amazon Comprehend DE", result.getNames().get(Language.DE.getValue()),
        "The German name should be correctly mapped from the GHContent.");
    Assertions.assertTrue(result.getDeprecated(), "The product should be marked as deprecated.");
    Assertions.assertFalse(CollectionUtils.isEmpty(result.getArtifacts()), "Product artifacts should not be empty.");
    Assertions.assertFalse(result.getArtifacts().get(0).isInvalidArtifact(),
        "The first artifact should be valid.");
  }

  @Test
  void testMappingLogo() {
    Product product = new Product();
    GHContent content = mock(GHContent.class);
    when(content.getName()).thenReturn(ProductJsonConstants.LOGO_FILE);
    var result = ProductFactory.mappingByGHContent(product, content);
    assertNotNull(result, "Mapping with a logo file should not return null.");

    when(content.getName()).thenReturn(ProductJsonConstants.LOGO_FILE);
    result = ProductFactory.mappingByGHContent(product, content);
    assertNotNull(result, "Mapping with the logo file a second time should still not return null.");
  }

  @Test
  void testExtractSourceUrl() {
    Product product = new Product();
    Meta meta = new Meta();
    ProductFactory.extractSourceUrl(product, meta);
    Assertions.assertNull(product.getRepositoryName(), "Repository name should be null when source URL is not set.");
    Assertions.assertNull(product.getSourceUrl(), "Source URL should be null when meta source URL is not set.");

    String sourceUrl = "https://github.com/axonivy-market/alfresco-connector";
    meta.setSourceUrl(sourceUrl);
    ProductFactory.extractSourceUrl(product, meta);
    Assertions.assertEquals("axonivy-market/alfresco-connector", product.getRepositoryName(),
        "Repository name should be extracted correctly from a full GitHub URL.");
    Assertions.assertEquals(sourceUrl, product.getSourceUrl(),
        "Source URL should match the meta source URL.");

    sourceUrl = "portal";
    meta.setSourceUrl(sourceUrl);
    ProductFactory.extractSourceUrl(product, meta);
    Assertions.assertEquals(sourceUrl, product.getRepositoryName(),
        "Repository name should match the source URL if it is not a full GitHub URL.");
    Assertions.assertEquals(sourceUrl, product.getSourceUrl(),
        "Source URL should match the meta source URL when it is a simple string.");
  }

  @Test
  void testTransferComputedData() {
    Product product = new Product();
    Product persistedData = new Product();
    persistedData.setMarketDirectory(SAMPLE_PRODUCT_PATH);
    persistedData.setLogoId(SAMPLE_LOGO_ID);

    ProductFactory.transferComputedPersistedDataToProduct(persistedData, product);

    assertEquals(SAMPLE_PRODUCT_PATH, product.getMarketDirectory(),
        "Market directory should be transferred correctly from persisted data to product.");
    assertEquals(SAMPLE_LOGO_ID, product.getLogoId(),
        "Logo ID should be transferred correctly from persisted data to product.");
  }
}