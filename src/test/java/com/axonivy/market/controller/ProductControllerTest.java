package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpStatus;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
  private static final String PRODUCT_NAME_SAMPLE = "Amazon Comprehend";
  private static final String PRODUCT_DESC_SAMPLE = "Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.";

  @Mock
  private ProductService service;

  @Mock
  private ProductModelAssembler assembler;

  @Mock
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @Mock
  private PagedModel<?> pagedProductModel;

  @InjectMocks
  private ProductController productController;

  @BeforeEach
  void setup() {
    assembler = new ProductModelAssembler();
  }

  @Test
  void testFindProductsAsEmpty() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<Product> mockProducts = new PageImpl<Product>(List.of(), pageable, 0);
    when(service.findProducts(any(), any(), any())).thenReturn(mockProducts);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());
    var result = productController.findProducts(FilterType.ALL.getOption(), null, pageable);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(0, result.getBody().getContent().size());
  }

  @Test
  void testFindProducts() {
    PageRequest pageable = PageRequest.of(0, 20, Sort.by(Order.by(SortOption.ALPHABETICALLY.getOption())));
    Product mockProduct = createProductMock();

    Page<Product> mockProducts = new PageImpl<Product>(List.of(mockProduct), pageable, 1);
    when(service.findProducts(any(), any(), any())).thenReturn(mockProducts);
    assembler = new ProductModelAssembler();
    var mockProductModel = assembler.toModel(mockProduct);
    var mockPagedModel = PagedModel.of(List.of(mockProductModel), new PageMetadata(1, 0, 1));
    when(pagedResourcesAssembler.toModel(any(), any(ProductModelAssembler.class))).thenReturn(mockPagedModel);

    var result = productController.findProducts(FilterType.ALL.getOption(), null, pageable);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, result.getBody().getContent().size());
    assertEquals(PRODUCT_NAME_SAMPLE, result.getBody().getContent().iterator().next().getName());
  }

  private Product createProductMock() {
    Product mockProduct = new Product();
    mockProduct.setId("amazon-comprehend");
    mockProduct.setName(PRODUCT_NAME_SAMPLE);
    mockProduct.setShortDescription(PRODUCT_DESC_SAMPLE);
    mockProduct.setType("connector");
    mockProduct.setTags(List.of("AI"));
    return mockProduct;
  }
}
