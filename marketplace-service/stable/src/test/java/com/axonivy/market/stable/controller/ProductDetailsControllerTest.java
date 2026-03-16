package com.axonivy.market.stable.controller;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductJsonContent;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.core.model.ProductDetailModel;
import com.axonivy.market.stable.assembler.ProductDetailModelAssembler;
import com.axonivy.market.stable.service.ProductService;
import com.axonivy.market.stable.service.VersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductDetailsControllerTest {

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
  public static final String DOCKER_CONNECTOR_ID = "docker-connector";
  protected static final String MOCK_RELEASED_VERSION = "10.0.10";
  protected static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  protected static final String MOCK_DESIGNER_VERSION = "12.0.4";

  @Test
  void testFindBestMatchProductDetailsByVersion() {
    when(productService.fetchBestMatchProductDetail(anyString(), anyString())).thenReturn(
        mockProduct());
    when(detailModelAssembler.toModel(mockProduct())).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findBestMatchProductDetailsByVersion(
        DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Expected response status code: " + result.getStatusCode() + " to match HTTP status 200 OK");
    assertEquals(mockExpectedResult, result,
        "ResponseEntity should match the expected result");

    verify(productService, times(1)).fetchBestMatchProductDetail(DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);
    verify(detailModelAssembler, times(1)).toModel(mockProduct());
  }

  @Test
  void testFindProductJsonContentByIdAndVersion() throws IOException {
    ProductJsonContent productJsonContent = mockProductJsonContent();
    Map<String, Object> map = new ObjectMapper().readValue(productJsonContent.getContent(), Map.class);
    when(versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION,
        MOCK_DESIGNER_VERSION)).thenReturn(
        map);

    var result = productDetailsController.findProductJsonContent(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION,
        MOCK_DESIGNER_VERSION);

    assertEquals(new ResponseEntity<>(map, HttpStatus.OK), result,
        "Expected ResponseEntity with provided map and HTTP 200 OK");
  }

  private Product mockProduct() {
    Product mockProduct = new Product();
    mockProduct.setId(DOCKER_CONNECTOR_ID);
    Map<String, String> name = new HashMap<>();
    name.put(Language.EN.getValue(), PRODUCT_NAME_SAMPLE);
    name.put(Language.DE.getValue(), PRODUCT_NAME_DE_SAMPLE);
    mockProduct.setNames(name);
    mockProduct.setLanguage("English");
    return mockProduct;
  }

  private ProductDetailModel createProductMockWithDetails() {
    ProductDetailModel mockProductDetail = new ProductDetailModel();
    mockProductDetail.setId(DOCKER_CONNECTOR_ID);
    Map<String, String> name = new HashMap<>();
    name.put(Language.EN.getValue(), PRODUCT_NAME_SAMPLE);
    name.put(Language.DE.getValue(), PRODUCT_NAME_DE_SAMPLE);
    mockProductDetail.setNames(name);
    mockProductDetail.setType("connector");
    mockProductDetail.setSourceUrl("https://github.com/axonivy-market/docker-connector");
    mockProductDetail.setStatusBadgeUrl("https://github.com/axonivy-market/docker-connector");
    mockProductDetail.setLanguage("English");
    mockProductDetail.setIndustry("Cross-Industry");
    mockProductDetail.setContactUs(false);
    return mockProductDetail;
  }

  private ProductJsonContent mockProductJsonContent() {
    String encodedContent = """
        {
            "$schema": "https://json-schema.axonivy.com/market/10.0.0/product.json",
            "minimumIvyVersion": "10.0.8",
            "installers": [
                {
                    "id": "maven-import",
                    "data": {
                        "projects": [
                            {
                                "groupId": "com.axonivy.utils.docfactory",
                                "artifactId": "aspose-barcode-demo",
                                "version": "${version}",
                                "type": "iar"
                            }
                        ],
                        "repositories": [
                            {
                                "id": "maven.axonivy.com",
                                "url": "https://maven.axonivy.com"
                            }
                        ]
                    }
                }
            ]
        }
        """;

    ProductJsonContent jsonContent = new ProductJsonContent();
    jsonContent.setContent(encodedContent);
    jsonContent.setName("aspose-barcode");

    return jsonContent;
  }
}
