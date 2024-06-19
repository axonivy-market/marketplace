package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

@Log4j2
@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {
  @Mock
  private ProductService service;

  @Mock
  private ProductDetailModelAssembler productDetailAssembler;
  @InjectMocks
  private ProductDetailsController productDetailsController;

  @BeforeEach
  void setup(){
    productDetailAssembler = new ProductDetailModelAssembler();
  }

//  @Test
//  public void testFindProduct() throws Exception {
//    ProductDetailModel productDetailModel = new ProductDetailModel();
//    when(service.fetchProductDetail(anyString(), any())).thenReturn(productDetailModel);
//    when(detailModelAssembler.toModel(any())).thenReturn(productDetailModel);
//
//    mockMvc.perform(MockMvcRequestBuilders.get("/1")
//                    .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//  }
//
//  @Test
//  public void testGetReadmeAndProductContentsFromTag() throws Exception {
//    ReadmeModel readmeModel = new ReadmeModel();
//    when(service.getReadmeAndProductContentsFromTag(anyString(), anyString())).thenReturn(readmeModel);
//
//    mockMvc.perform(MockMvcRequestBuilders.get("/1/readme")
//                    .param("tag", "v1.0")
//                    .contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//  }
//  @Test
//  public void testFindProductWithValidIdAndType() {
//    String id = "docker-connector";
//    String type = "connector";
//
//    Product product = createProductMockWithDetails();
//    var mockModel = productDetailAssembler.toModel(product);
//    when(service.fetchProductDetail(id, type)).thenReturn(product);
//    when(productDetailAssembler.toModel(product)).thenReturn(mockModel);
//
//   var result = productDetailsController.findProduct(id, type);
//    assertEquals(HttpStatus.OK, result.getStatusCode());
//    assertTrue(result.hasBody());
//  }

//  @Test
//  public void testFindProductWithValidIdNoType() {
//    String id = "123";
//    ProductDetail productDetail = new ProductDetail();
//    ProductDetailModel model = new ProductDetailModel();
//
//    when(service.fetchProductDetail(id, null)).thenReturn(productDetail);
//    when(detailModelAssembler.toModel(productDetail)).thenReturn(model);
//
//    ResponseEntity<ProductDetailModel> response = controller.findProduct(id, null);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(model, response.getBody());
//  }
//
//  @Test public void testGetReadmeAndProductContentsFromTagValidInputs() {
//    String id = "123";
//    String tag = "v1.0";
//    ReadmeModel readmeModel = new ReadmeModel();
//    when(service.getReadmeAndProductContentsFromTag(id, tag)).thenReturn(readmeModel);
//
//    ResponseEntity<ReadmeModel> response = controller.getReadmeAndProductContentsFromTag(id, tag);
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(readmeModel, response.getBody());
//  }
//
//  @Test public void testGetReadmeAndProductContentsFromTagWithNullTag() {
//    String id = "123";
//
//    ResponseEntity<ReadmeModel> response = controller.getReadmeAndProductContentsFromTag(id, null);
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
//
//  @Test public void testGetReadmeAndProductContentsFromTagWithEmptyTag() {
//    String id = "123";
//    ResponseEntity<ReadmeModel> response = controller.getReadmeAndProductContentsFromTag(id, "");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
private Product createProductMockWithDetails(){
    Product mockProduct = new Product();
  mockProduct.setId("docker-connector");
  mockProduct.setName("Docker");
  mockProduct.setShortDescription("Create, start, stop, remove Docker containers directly from your business processes.");
  mockProduct.setType("connector");
  mockProduct.setTags(List.of("container"));
  mockProduct.setCompatibility("10.0+");
  mockProduct.setSourceUrl("https://github.com/axonivy-market/docker-connector");
  mockProduct.setStatusBadgeUrl("https://github.com/axonivy-market/docker-connector");
  mockProduct.setLanguage("English");
  mockProduct.setIndustry("Cross-Industry");
  mockProduct.setContactUs(false);
  return mockProduct;
}
}
