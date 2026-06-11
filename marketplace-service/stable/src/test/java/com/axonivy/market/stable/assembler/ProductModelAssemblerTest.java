package com.axonivy.market.stable.assembler;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.ProductModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProductModelAssemblerTest {

  @InjectMocks
  private ProductModelAssembler productModelAssembler;

  @Test
  void testToModel() {
    Product product = new Product();
    product.setId("test-id");
    product.setNames(Map.of("en", "Test Name"));
    product.setShortDescriptions(Map.of("en", "Test Description"));
    product.setType("connector");
    product.setTags(List.of("tag1", "tag2"));
    product.setMarketDirectory("test/directory/");
    product.setLogoId("logo-123");

    ProductModel model = productModelAssembler.toModel(product);

    assertNotNull(model);
    assertEquals(product.getId(), model.getId());
    assertEquals(product.getNames(), model.getNames());
    assertEquals(product.getShortDescriptions(), model.getShortDescriptions());
    assertEquals(product.getType(), model.getType());
    assertEquals(product.getTags(), model.getTags());
    assertEquals(product.getMarketDirectory(), model.getMarketDirectory());
    assertNotNull(model.getLogoUrl());
    assertTrue(model.getLogoUrl().contains("logo-id"),
        "logo ID should be included in Url when logoId is present");
    assertNull(model.getLogoDarkUrl(), "Logo Dark URL should be null when logoDarkId is not present");
  }

  @Test
  void testToModelShouldSetDarkLogoUrlWhenLogoDarkIdExists() {
    Product product = new Product();
    product.setId("product-id");
    product.setLogoId("logo-id");
    product.setLogoDarkId("logo-dark-id");

    ProductModel model = productModelAssembler.toModel(product);

    assertNotNull(model.getLogoDarkUrl(), "Dark logo URL should be set when logoDarkId is present");
    assertTrue(model.getLogoDarkUrl().contains("logo-dark-id"),
        "Dark logo ID should be included in Url when logoDarkId is present");
  }
}
