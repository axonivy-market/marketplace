package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Log4j2
@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {
    @Mock
    private ProductService service;

    @Mock
    private ProductModelAssembler productAssembler;

    @Mock
    private ProductDetailModelAssembler detailModelAssembler;

    @InjectMocks
    private ProductDetailsController productDetailsController;

    @Test
    void testProductDetails() {
        Mockito.when(service.fetchProductDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(mockProduct());
        Mockito.when(detailModelAssembler.toModel(Mockito.any())).thenReturn(createProductMockWithDetails());
        ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(), HttpStatus.OK);

        ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails("docker-connector", "connector");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(result, mockExpectedResult);

        verify(service, times(1)).fetchProductDetail("docker-connector", "connector");
        verify(detailModelAssembler, times(1)).toModel(mockProduct());
    }

    private Product mockProduct() {
        return Product.builder().id("docker-connector").name("Docker").language("English").build();
    }

    private ProductDetailModel createProductMockWithDetails() {
        ProductDetailModel mockProduct = new ProductDetailModel();
        mockProduct.setId("docker-connector");
        mockProduct.setName("Docker");
        mockProduct.setType("connector");
        mockProduct.setCompatibility("10.0+");
        mockProduct.setSourceUrl("https://github.com/axonivy-market/docker-connector");
        mockProduct.setStatusBadgeUrl("https://github.com/axonivy-market/docker-connector");
        mockProduct.setLanguage("English");
        mockProduct.setIndustry("Cross-Industry");
        mockProduct.setContactUs(false);
        return mockProduct;
    }
}
