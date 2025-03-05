package com.axonivy.market.assembler;

import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductDetailModelAssemblerTest {
  private static final String ID = "portal";

  Product mockProduct;
  @InjectMocks
  private ProductDetailModelAssembler productDetailModelAssembler;

  @BeforeEach
  void setup() {
    productDetailModelAssembler = new ProductDetailModelAssembler();
    mockProduct = new Product();
    mockProduct.setId(ID);
    mockProduct.setReleasedVersions(List.of("11.0.1", "10.0.8"));
  }

  @Test
  void testToModel() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct);
    Assertions.assertEquals(ID, model.getId());
    Assertions.assertTrue(model.getLinks().isEmpty());
  }

}