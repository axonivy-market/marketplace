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
  }

  @Test
  void testToModel() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct);
    Assertions.assertEquals(ID, model.getId());
    Assertions.assertFalse(model.getLinks().isEmpty());
    Assertions.assertEquals("http://localhost/api/product-details/portal", model.getLink(SELF_RELATION).get().getHref());
  }

  @Test
  void testToModelWithRequestPath() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct, RequestMappingConstants.BY_ID);
    Assertions.assertEquals("http://localhost/api/product-details/portal", model.getLink(SELF_RELATION).get().getHref());
  }

  @Test
  void testToModelWithRequestPathAndVersion() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct, VERSION, RequestMappingConstants.BY_ID_AND_VERSION);
    Assertions.assertEquals("http://localhost/api/product-details/portal/10.0.19", model.getLink(SELF_RELATION).get().getHref());
  }

  @Test
  void testToModelWithRequestPathAndBestMatchVersion() {
    ProductDetailModel model = productDetailModelAssembler.toModel(mockProduct, VERSION, RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION);
    Assertions.assertEquals("http://localhosts/api/product-details/portal/10.0.19/bestmatch", model.getLink(SELF_RELATION).get().getHref());
  }
}