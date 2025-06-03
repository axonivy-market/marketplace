package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.GithubReleaseModelAssembler;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.bo.VersionDownload;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest extends BaseSetup {
  @Mock
  private ProductService productService;
  @Mock
  VersionService versionService;
  @Mock
  private ProductDetailModelAssembler detailModelAssembler;
  @Mock
  private PagedResourcesAssembler<GitHubReleaseModel> pagedResourcesAssembler;
  @Mock
  private GithubReleaseModelAssembler githubReleaseModelAssembler;
  @Mock
  private ProductContentService productContentService;

  @InjectMocks
  private ProductDetailsController productDetailsController;
  private static final String PRODUCT_NAME_SAMPLE = "Docker";
  private static final String PRODUCT_NAME_DE_SAMPLE = "Docker DE";
  public static final String DOCKER_CONNECTOR_ID = "docker-connector";
  public static final String WRONG_PRODUCT_ID = "wrong-product-id";

  @Test
  void testProductDetails() {
    when(productService.fetchProductDetail(anyString(), anyBoolean())).thenReturn(mockProduct());
    when(detailModelAssembler.toModel(mockProduct())).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails(DOCKER_CONNECTOR_ID, false);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchProductDetail(DOCKER_CONNECTOR_ID, false);
    verify(detailModelAssembler).toModel(mockProduct());
    assertTrue(result.hasBody());
    assertEquals(DOCKER_CONNECTOR_ID, Objects.requireNonNull(result.getBody()).getId());
  }


  @Test
  void testFindBestMatchProductDetailsByVersion() {
    when(productService.fetchBestMatchProductDetail(anyString(), anyString())).thenReturn(
        mockProduct());
    when(detailModelAssembler.toModel(mockProduct())).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findBestMatchProductDetailsByVersion(
        DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchBestMatchProductDetail(DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION);
    verify(detailModelAssembler, times(1)).toModel(mockProduct());
  }

  @Test
  void testProductDetailsWithVersion() {
    when(productService.fetchProductDetailByIdAndVersion(anyString(), anyString())).thenReturn(
        mockProduct());
    when(detailModelAssembler.toModel(mockProduct())).thenReturn(createProductMockWithDetails());
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
    when(productService.fetchProductDetailByIdAndVersion(anyString(), anyString())).thenReturn(
        null);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetailsByVersion(
        WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productService, times(1)).fetchProductDetailByIdAndVersion(WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testBestMatchProductDetailsWithVersionWithWrongProductId() {
    when(productService.fetchBestMatchProductDetail(anyString(), anyString())).thenReturn(
        null);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findBestMatchProductDetailsByVersion(
        WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productService, times(1)).fetchBestMatchProductDetail(WRONG_PRODUCT_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testProductDetailsWithWrongProductId() {
    when(productService.fetchProductDetail(anyString(), anyBoolean())).thenReturn(
        null);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails(
        WRONG_PRODUCT_ID, false);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productService, times(1)).fetchProductDetail(WRONG_PRODUCT_ID, false);
  }

  @Test
  void testFindProductVersionsById() {
    List<MavenArtifactVersionModel> models = List.of(new MavenArtifactVersionModel());
    when(versionService.getArtifactsAndVersionToDisplay(anyString(), anyBoolean(),
        anyString()))
        .thenReturn(models);
    ResponseEntity<List<MavenArtifactVersionModel>> result = productDetailsController.findProductVersionsById("portal",
        true, "10.0.1");
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    assertEquals(models, result.getBody());
  }

  @Test
  void findProductVersionsById() {
    when(versionService.getVersionsForDesigner("google-maps-connector", true, StringUtils.EMPTY))
        .thenReturn(mockVersionAndUrlModels());

    var result = productDetailsController.findVersionsForDesigner("google-maps-connector", StringUtils.EMPTY, true);

    assertEquals(2, Objects.requireNonNull(result.getBody()).size());
    assertEquals("10.0.21", Objects.requireNonNull(result.getBody()).get(0).getVersion());
    assertEquals("/api/product-details/portal/10.0.21/json",
        Objects.requireNonNull(result.getBody()).get(0).getUrl());
    assertEquals("10.0.22", Objects.requireNonNull(result.getBody()).get(1).getVersion());
    assertEquals("/api/product-details/portal/10.0.22/json",
        Objects.requireNonNull(result.getBody()).get(1).getUrl());
  }

  @Test
  void findProductJsonContentByIdAndVersion() throws IOException {
    ProductJsonContent productJsonContent = mockProductJsonContent();
    Map<String, Object> map = new ObjectMapper().readValue(productJsonContent.getContent(), Map.class);
    when(versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION,
        MOCK_DESIGNER_VERSION)).thenReturn(
        map);

    var result = productDetailsController.findProductJsonContent(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION,
        MOCK_DESIGNER_VERSION);

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
    when(versionService.getLatestVersionArtifactDownloadUrl(anyString(), anyString(),
        anyString())).thenReturn(StringUtils.EMPTY);
    var response = productDetailsController.getLatestArtifactDownloadUrl("portal", "1.0.0", "portal-app.zip");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    when(versionService.getLatestVersionArtifactDownloadUrl(anyString(), anyString(),
        anyString())).thenReturn(mockDownloadUrl);
    response = productDetailsController.getLatestArtifactDownloadUrl("portal", "1.0.0", "portal-app.zip");
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testFindGithubPublicReleaseByProductIdAndReleaseId() throws IOException {
    GitHubReleaseModel githubReleaseModel = new GitHubReleaseModel();
    when(productService.getGitHubReleaseModelByProductIdAndReleaseId(anyString(), anyLong()))
        .thenReturn(githubReleaseModel);
    when(githubReleaseModelAssembler.toModel(any(GitHubReleaseModel.class))).thenReturn(githubReleaseModel);

    var result = productDetailsController.findGithubPublicReleaseByProductIdAndReleaseId("portal", 1L);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(githubReleaseModel, result.getBody());
  }

  @Test
  void testFindGithubPublicReleases() throws IOException {
    Page<GitHubReleaseModel> page = new PageImpl<>(List.of(new GitHubReleaseModel()));
    when(productService.getGitHubReleaseModels(anyString(), any(Pageable.class))).thenReturn(page);
    when(pagedResourcesAssembler.toModel(any(Page.class), any(GithubReleaseModelAssembler.class)))
        .thenReturn(PagedModel.of(List.of(new GitHubReleaseModel()), new PagedModel.PageMetadata(1, 0, 1)));

    var result = productDetailsController.findGithubPublicReleases("portal", Pageable.ofSize(1));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size());
  }

  @Test
  void testFindGithubPublicReleasesWithEmptyResult() throws IOException {
    Page<GitHubReleaseModel> emptyPage = Page.empty();
    when(productService.getGitHubReleaseModels(anyString(), any(Pageable.class))).thenReturn(emptyPage);
    when(pagedResourcesAssembler.toEmptyModel(any(Page.class), any())).thenReturn(PagedModel.empty());

    var result = productDetailsController.findGithubPublicReleases("portal", Pageable.ofSize(1));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getBody()).getContent().isEmpty());
  }

  @Test
  void testSyncLatestReleasesForProducts() throws IOException {
    List<String> productIdList = List.of(DOCKER_CONNECTOR_ID);
    when(productService.getProductIdList()).thenReturn(productIdList);
    when(productService.syncGitHubReleaseModels(anyString(), any(Pageable.class))).thenReturn(
        Page.empty());

    productDetailsController.syncLatestReleasesForProducts();

    verify(productService, times(1)).getProductIdList();
  }

  @Test
  void testDownloadZipArtifact_NotFound() {
    when(productContentService.downloadZipArtifactFile(anyString(), anyString(),
        anyString())).thenReturn(null);

    var result = productDetailsController.downloadZipArtifact(
        DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION, "artifact");

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

    verify(productContentService, times(1)).downloadZipArtifactFile(DOCKER_CONNECTOR_ID, "artifact",
        MOCK_RELEASED_VERSION);
  }

  @Test
  void testDownloadZipArtifact() {
    when(productContentService.downloadZipArtifactFile(MOCK_PRODUCT_ID, MOCK_DEMO_ARTIFACT_ID, MOCK_RELEASED_VERSION))
        .thenReturn(new VersionDownload());
    var result = productDetailsController.downloadZipArtifact(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION,
        MOCK_DEMO_ARTIFACT_ID);
    assertNotNull(result);
    assertNotNull(result.getBody());
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }
}
