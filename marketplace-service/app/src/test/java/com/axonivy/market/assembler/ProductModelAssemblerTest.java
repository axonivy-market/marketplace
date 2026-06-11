package com.axonivy.market.assembler;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.ProductModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProductModelAssemblerTest {

  @InjectMocks
  private ProductModelAssembler productModelAssembler;

  @Test
  void testToModelShouldSetDarkLogoUrlWhenLogoDarkIdExists() {
    Product product = new Product();
    product.setId("test-id");
    product.setLogoId("logo-id");
    product.setLogoDarkId("logo-dark-id");

    ProductModel model = productModelAssembler.toModel(product);

    assertNotNull(model.getLogoDarkUrl(), "Dark logo URL should be set when logoDarkId is present");
    assertTrue(model.getLogoDarkUrl().contains("logo-dark-id"),
        "Dark logo ID should be included in Url when logoDarkId is present");
  }

  @Test
  void testToModelShouldNotSetDarkLogoUrlWhenLogoDarkIdIsBlank() {
    Product product = new Product();
    product.setId("test-id");
    product.setLogoId("logo-id");
    product.setLogoDarkId("   ");

    ProductModel model = productModelAssembler.toModel(product);

    assertNull(model.getLogoDarkUrl(), "Dark logo URL should be set when logoDarkId is blank");
  }
}
