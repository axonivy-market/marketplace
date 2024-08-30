package com.axonivy.market.assembler;

import com.axonivy.market.constants.RequestMappingConstants;
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
  private static final String VERSION = "10.0.19";
  private static final String SELF_RELATION = "self";

  Product mockProduct;
  @InjectMocks
  private ProductDetailModelAssembler productDetailModelAssembler;

  @BeforeEach
  void setup() {
    productDetailModelAssembler = new ProductDetailModelAssembler(new ProductModelAssembler());
    mockProduct = new Product();
    mockProduct.setId(ID);
    mockProduct.setReleasedVersions(List.of("11.0.1", "10.0.8"));
  }

  @Test
  void testToModel() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct);
    Assertions.assertEquals(ID, model.getId());
    Assertions.assertFalse(model.getLinks().isEmpty());
    Assertions.assertTrue(model.getLink(SELF_RELATION).get().getHref().endsWith("/api/product-details/portal"));
  }

  @Test
  void testToModelWithRequestPath() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct, RequestMappingConstants.BY_ID);
    Assertions.assertTrue(model.getLink(SELF_RELATION).get().getHref().endsWith("/api/product-details/portal"));
  }

  @Test
  void testToModelWithRequestPathAndVersion() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct, VERSION, RequestMappingConstants.BY_ID_AND_VERSION);
    Assertions.assertTrue(model.getLink(SELF_RELATION).get().getHref().endsWith("/api/product-details/portal/10.0.19"));
  }

  @Test
  void testToModelWithRequestPathAndBestMatchVersion() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct, VERSION, RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION);
    Assertions.assertTrue(model.getLink(SELF_RELATION).get().getHref().endsWith("/api/product-details/portal/10.0.19/bestmatch"));
    Assertions.assertTrue(model.getMetaProductJsonUrl().endsWith("/api/product-details/productjsoncontent/portal/10.0.8"));
  }
}