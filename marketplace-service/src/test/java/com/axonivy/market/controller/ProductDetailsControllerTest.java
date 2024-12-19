package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.constants.RequestMappingConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest extends BaseSetup {
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
  public static final String WRONG_PRODUCT_ID = "wrong-product-id";

  @Test
  void testProductDetails() {
    Mockito.when(productService.fetchProductDetail(Mockito.anyString(), anyBoolean())).thenReturn(mockProduct());
    Mockito.when(detailModelAssembler.toModel(mockProduct(), RequestMappingConstants.BY_ID)).thenReturn(
        createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails(DOCKER_CONNECTOR_ID, false);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchProductDetail(DOCKER_CONNECTOR_ID, false);
    verify(detailModelAssembler, times(1)).toModel(mockProduct(), RequestMappingConstants.BY_ID);
    assertTrue(result.hasBody());
    assertEquals(DOCKER_CONNECTOR_ID, Objects.requireNonNull(result.getBody()).getId());
  }


  @Test
  void testFindBestMatchProductDetailsByVersion() {
    Mockito.when(productService.fetchBestMatchProductDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(
        mockProduct());
    Mockito.when(detailModelAssembler.toModel(mockProduct(), MOCK_RELEASED_VERSION,
        RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION)).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findBestMatchProductDetailsByVersion(
        DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchBestMatchProductDetail(DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);
    verify(detailModelAssembler, times(1)).toModel(mockProduct(), MOCK_RELEASED_VERSION,
        RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION);
  }

  @Test
  void testProductDetailsWithVersion() {
    Mockito.when(productService.fetchProductDetailByIdAndVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(
        mockProduct());
    Mockito.when(
        detailModelAssembler.toModel(mockProduct(), MOCK_RELEASED_VERSION,
            RequestMappingConstants.BY_ID_AND_VERSION)).thenReturn(
        createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetailsByVersion(
        DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchProductDetailByIdAndVersion(DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testProductDetailsWithVersionWithWrongProductId() {
    Mockito.when(productService.fetchProductDetailByIdAndVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(
        null);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetailsByVersion(
        WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productService, times(1)).fetchProductDetailByIdAndVersion(WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testBestMatchProductDetailsWithVersionWithWrongProductId() {
    Mockito.when(productService.fetchBestMatchProductDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(
        null);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findBestMatchProductDetailsByVersion(
        WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productService, times(1)).fetchBestMatchProductDetail(WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testProductDetailsWithWrongProductId() {
    Mockito.when(productService.fetchProductDetail(Mockito.anyString(), anyBoolean())).thenReturn(
        null);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails(
        WRONG_PRODUCT_ID, false);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productService, times(1)).fetchProductDetail(WRONG_PRODUCT_ID, false);
  }

  @Test
  void testFindProductVersionsById() {
    List<MavenArtifactVersionModel> models = List.of(new MavenArtifactVersionModel());
    Mockito.when(
            versionService.getArtifactsAndVersionToDisplay(Mockito.anyString(), Mockito.anyBoolean(),
                    Mockito.anyString()))
        .thenReturn(models);
    ResponseEntity<List<MavenArtifactVersionModel>> result = productDetailsController.findProductVersionsById("portal",
        true, "10.0.1");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    Assertions.assertEquals(models, result.getBody());
  }

  @Test
  void findProductVersionsById() {
    when(versionService.getVersionsForDesigner("google-maps-connector")).thenReturn(mockVersionAndUrlModels());

    var result = productDetailsController.findVersionsForDesigner("google-maps-connector");

    assertEquals(2, Objects.requireNonNull(result.getBody()).size());
    assertEquals("10.0.21", Objects.requireNonNull(result.getBody()).get(0).getVersion());
    assertEquals("/api/product-details/productjsoncontent/portal/10.0.21",
        Objects.requireNonNull(result.getBody()).get(0).getUrl());
    assertEquals("10.0.22", Objects.requireNonNull(result.getBody()).get(1).getVersion());
    assertEquals("/api/product-details/productjsoncontent/portal/10.0.22",
        Objects.requireNonNull(result.getBody()).get(1).getUrl());
  }



  @Test
  void findProductJsonContentByIdAndVersion() throws IOException {
    ProductJsonContent productJsonContent = mockProductJsonContent();
    Map<String, Object> map = new ObjectMapper().readValue(productJsonContent.getContent(), Map.class);
    when(versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(
        map);

    var result = productDetailsController.findProductJsonContent(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(new ResponseEntity<>(map, HttpStatus.OK), result);
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
    mockProductDetail.setCompatibility("10.0+");
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

  @Test
  void testGetLatestArtifactDownloadUrl() {
    String mockDownloadUrl = "https://market.axonivy.com";
    when(versionService.getLatestVersionArtifactDownloadUrl(Mockito.anyString(),Mockito.anyString(),
        Mockito.anyString())).thenReturn(StringUtils.EMPTY);
    var response = productDetailsController.getLatestArtifactDownloadUrl("portal", "1.0.0", "portal-app.zip");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    when(versionService.getLatestVersionArtifactDownloadUrl(Mockito.anyString(),Mockito.anyString(),
        Mockito.anyString())).thenReturn(mockDownloadUrl);
    response = productDetailsController.getLatestArtifactDownloadUrl("portal", "1.0.0", "portal-app.zip");
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
