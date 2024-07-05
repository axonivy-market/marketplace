package com.axonivy.market.factory;

import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Product;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {
  private static final String DUMMY_LOGO_URL = "https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/amazon-comprehend-connector/logo.png";

  @Test
  void testMappingByGHContent() throws IOException {
    Product product = new Product();
    GHContent mockContent = mock(GHContent.class);
    when(mockContent.getName()).thenReturn(CommonConstants.META_FILE);
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);
    var result = ProductFactory.mappingByGHContent(product, mockContent);
    assertNotEquals(null, result);
    assertEquals("Amazon Comprehend", result.getName());
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
}
