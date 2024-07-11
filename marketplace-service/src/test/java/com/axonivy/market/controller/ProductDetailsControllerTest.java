package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.MultilingualismValue;
import com.axonivy.market.service.VersionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {
  @Mock
  private ProductService productService;

  @Mock
  VersionService versionService;

  @Mock
  private ProductDetailModelAssembler detailModelAssembler;

  @InjectMocks
  private ProductDetailsController productDetailsController;
  private static final String PRODUCT_NAME_SAMPLE = "Docker";
  private static final String PRODUCT_NAME_DE_SAMPLE = "Docker DE";

  @Test
  void testProductDetails() {
    Mockito.when(productService.fetchProductDetail(Mockito.anyString())).thenReturn(mockProduct());
    Mockito.when(detailModelAssembler.toModel(mockProduct(), null)).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult =
        new ResponseEntity<>(createProductMockWithDetails(), HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails("docker-connector");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchProductDetail("docker-connector");
    verify(detailModelAssembler, times(1)).toModel(mockProduct(), null);
  }

  @Test
  void testFindProductVersionsById() {
    List<MavenArtifactVersionModel> models = List.of(new MavenArtifactVersionModel());
    Mockito.when(
        versionService.getArtifactsAndVersionToDisplay(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString()))
        .thenReturn(models);
    ResponseEntity<List<MavenArtifactVersionModel>> result =
        productDetailsController.findProductVersionsById("protal", true, "10.0.1");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    Assertions.assertEquals(models, result.getBody());
  }

  private Product mockProduct() {
    Product mockProduct = new Product();
    mockProduct.setId("docker-connector");
    MultilingualismValue name = new MultilingualismValue();
    name.setEn(PRODUCT_NAME_SAMPLE);
    name.setDe(PRODUCT_NAME_DE_SAMPLE);
    mockProduct.setNames(name);
    mockProduct.setLanguage("English");
    return mockProduct;
  }

  private ProductDetailModel createProductMockWithDetails() {
    ProductDetailModel mockProductDetail = new ProductDetailModel();
    mockProductDetail.setId("docker-connector");
    MultilingualismValue name = new MultilingualismValue();
    name.setEn(PRODUCT_NAME_SAMPLE);
    name.setDe(PRODUCT_NAME_DE_SAMPLE);
    mockProductDetail.setNames(name);
    mockProductDetail.setType("connector");
    mockProductDetail.setCompatibility("10.0+");
    mockProductDetail.setSourceUrl("https://github.com/axonivy-market/docker-connector");
    mockProductDetail.setStatusBadgeUrl("https://github.com/axonivy-market/docker-connector");
    mockProductDetail.setLanguage("English");
    mockProductDetail.setIndustry("Cross-Industry");
    mockProductDetail.setContactUs(false);
    return mockProductDetail;
  }
}
