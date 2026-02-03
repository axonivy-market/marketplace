package com.axonivy.market.stable.controller;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.model.ProductModel;
import com.axonivy.market.core.service.CoreProductService;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.stable.assembler.ProductModelAssembler;
import com.axonivy.market.stable.service.VersionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock
  private VersionService versionService;

  @Mock
  private CoreProductService coreProductService;

  @Mock
  private ProductModelAssembler assembler;

  @Mock
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @InjectMocks
  private ProductController productController;

  @Test
  void testFindProductJsonContent() {
    when(versionService.getProductJsonContentByIdAndVersion(anyString(), anyString()))
        .thenReturn(Map.of("key", "value"));

    ResponseEntity<?> result = productController.findProductJsonContent("test-id", "10.0.0");

    assertEquals(HttpStatus.OK, result.getStatusCode(), "Expected HTTP 200 OK");
    assertNotNull(result.getBody(), "Expected response body to not be null");
  }

  @Test
  void testFindProductVersionsById() {
    when(versionService.getArtifactsAndVersionToDisplay(anyString(), anyBoolean(), anyString()))
        .thenReturn(List.of(new MavenArtifactVersionModel()));

    ResponseEntity<?> result = productController.findProductVersionsById("test-id", true, "10.0.0");

    assertEquals(HttpStatus.OK, result.getStatusCode(), "Expected HTTP 200 OK");
    assertNotNull(result.getBody(), "Expected response body to not be null");
  }

  @Test
  void testFindProducts() {
    Product product = new Product();
    Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

    when(coreProductService.findProducts(any(), any(), any(), any(), any()))
        .thenReturn(productPage);
    doReturn(PagedModel.of(
        List.of(new ProductModel()),
        new PagedModel.PageMetadata(1, 0, 1)
    )).when(pagedResourcesAssembler).toModel(any(Page.class), any(ProductModelAssembler.class));

    ResponseEntity<?> result = productController.findProducts("all", "test", "en", PageRequest.of(0, 20));

    assertEquals(HttpStatus.OK, result.getStatusCode(), "Expected HTTP 200 OK");
    assertNotNull(result.getBody(), "Expected response body to not be null");
  }

  @Test
  void testFindProductsEmpty() {
    when(coreProductService.findProducts(any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    doReturn(PagedModel.of(
        List.of(),
        new PagedModel.PageMetadata(0, 0, 0)
    )).when(pagedResourcesAssembler).toEmptyModel(any(Page.class), any());

    ResponseEntity<?> result = productController.findProducts(null, null, null, PageRequest.of(0, 20));

    assertEquals(HttpStatus.OK, result.getStatusCode(), "Expected HTTP 200 OK");
    assertNotNull(result.getBody(), "Expected response body to not be null");
  }
}
