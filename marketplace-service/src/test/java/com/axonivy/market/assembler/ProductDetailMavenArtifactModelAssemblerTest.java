package com.axonivy.market.assembler;

import com.axonivy.market.config.MarketplaceConfig;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductDetailModelAssemblerTest {
  private static final String ID = "portal";
  @Mock
  MarketplaceConfig marketplaceConfig;
  Product mockProduct;
  @InjectMocks
  private ProductDetailModelAssembler productDetailModelAssembler;

  @BeforeEach
  void setup() {
    mockProduct = new Product();
    mockProduct.setId(ID);
    mockProduct.setReleasedVersions(List.of("11.0.1", "10.0.8"));
  }

  @Test
  void testToModel() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct);
    Assertions.assertEquals(ID, model.getId(), "The model ID should match the expected product ID.");
    Assertions.assertTrue(model.getLinks().isEmpty(), "The model links should be empty.");
  }
}
