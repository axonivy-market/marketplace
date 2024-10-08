package com.axonivy.market.factory;

import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.Language;
import com.axonivy.market.github.model.Meta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {
  private static final String DUMMY_LOGO_URL = "https://raw.githubusercontent" +
      ".com/axonivy-market/market/master/market/connector/amazon-comprehend-connector/logo.png";

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
    assertEquals("Amazon Comprehend", result.getNames().get(Language.EN.getValue()));
    assertEquals("Amazon Comprehend DE", result.getNames().get(Language.DE.getValue()));
    Assertions.assertFalse(CollectionUtils.isEmpty(result.getArtifacts()));
    Assertions.assertFalse(result.getArtifacts().get(0).isInvalidArtifact());
  }

  @Test
  void testMappingLogo() {
    Product product = new Product();
    GHContent content = mock(GHContent.class);
    when(content.getName()).thenReturn(ProductJsonConstants.LOGO_FILE);
    var result = ProductFactory.mappingByGHContent(product, content);
    assertNotEquals(null, result);

    when(content.getName()).thenReturn(ProductJsonConstants.LOGO_FILE);
    result = ProductFactory.mappingByGHContent(product, content);
    assertNotEquals(null, result);
  }

  @Test
  void testExtractSourceUrl() {
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

  @Test
  void testTransferComputedData() {
    String initialVersion = "10.0.2";
    Product product = new Product();
    Product persistedData = new Product();
    persistedData.setCustomOrder(1);
    persistedData.setReleasedVersions(List.of(initialVersion));
    persistedData.setNewestReleaseVersion(initialVersion);

    ProductFactory.transferComputedPersistedDataToProduct(persistedData, product);
    assertEquals(1, product.getCustomOrder());
    assertEquals(initialVersion, product.getNewestReleaseVersion());
    assertEquals(1, product.getReleasedVersions().size());
    assertEquals(initialVersion, product.getReleasedVersions().get(0));
  }
}