package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.ReadmeModel;
import com.axonivy.market.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
    private ProductDetailModelAssembler productDetailAssembler;

    @InjectMocks
    private ProductDetailsController productDetailsController;

    @BeforeEach
    void setup() {
        productAssembler = new ProductModelAssembler();
        productDetailAssembler = new ProductDetailModelAssembler(productAssembler);
    }

    @Test
    public void testFindProductDetails() {
        String id = "docker-connector";
        String type = "connector";

        Product product = createProductMockWithDetails();

        // Mock the service to return the product when fetchProductDetail is called
        when(service.fetchProductDetail(id, type)).thenReturn(product);

        // Create a mock ProductDetailModel
        ProductDetailModel mockProductDetailModel = mock(ProductDetailModel.class);
        ProductDetailModel model = productDetailAssembler.toModel(product);
        when(productDetailAssembler.toModel(any())).thenReturn(model);
        ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails(id, type);

        // Assertions
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockProductDetailModel, result.getBody());
        assertEquals("docker-connector", result.getBody().getId());
        assertEquals("Docker", result.getBody().getName());
        assertEquals("Create, start, stop, remove Docker containers directly from your business processes.", result.getBody().getShortDescription());
        assertEquals("connector", result.getBody().getType());

        // Verify interactions with the mocks
        verify(service, times(1)).fetchProductDetail(id, type);
        verify(productDetailAssembler, times(1)).toModel(product);
    }

    @Test
    void testGetReadmeAndProductContentsFromTag() {
        String productId = "amazon-comprehend";
        String tag = "v1.0";
        ReadmeModel mockReadmeModel = new ReadmeModel();
        when(service.getReadmeAndProductContentsFromTag(productId, tag)).thenReturn(mockReadmeModel);
        var result = productDetailsController.getReadmeAndProductContentsFromTag(productId, tag);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.hasBody());
    }

    private Product createProductMockWithDetails() {
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
