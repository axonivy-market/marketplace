package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductCustomSort;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.enums.SortOption;
import com.axonivy.market.core.exceptions.model.InvalidParamException;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.enums.PullRequestAction;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.DeprecatedRequest;
import com.axonivy.market.model.DeprecatedResponse;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.model.ProductDeprecationProjection;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import org.kohsuke.github.GHPullRequest;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMarketplaceDataServiceImplTest extends BaseSetup {
  @Mock
  private ProductRepository productRepo;
  @Mock
  private ProductCustomSortRepository productCustomSortRepo;
  @Mock
  private ProductMarketplaceDataRepository productMarketplaceDataRepo;
  @Mock
  private ProductDesignerInstallationRepository productDesignerInstallationRepo;
  @Mock
  private FileDownloadService fileDownloadService;
  @Mock
  private GitHubService gitHubService;
  @InjectMocks
  private ProductMarketplaceDataServiceImpl productMarketplaceDataService;
  @Captor
  ArgumentCaptor<ArrayList<ProductMarketplaceData>> productListArgumentCaptor;
  @Mock
  MavenArtifactVersionRepository mavenArtifactVersionRepo;

  @Test
  void testAddCustomSortProduct() throws InvalidParamException {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    ProductCustomSortRequest customSortRequest = new ProductCustomSortRequest();
    customSortRequest.setOrderedListOfProducts(orderedListOfProducts);
    customSortRequest.setRuleForRemainder(SortOption.ALPHABETICALLY.getOption());

    ProductMarketplaceData mockProductMarketplaceData = new ProductMarketplaceData();
    mockProductMarketplaceData.setId(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));

    productMarketplaceDataService.addCustomSortProduct(customSortRequest);

    verify(productCustomSortRepo).deleteAll();
    verify(productMarketplaceDataRepo).resetCustomOrderForAllProducts();
    verify(productCustomSortRepo).save(any(ProductCustomSort.class));
    verify(productMarketplaceDataRepo).saveAll(productListArgumentCaptor.capture());

    List<ProductMarketplaceData> capturedProducts = productListArgumentCaptor.getValue();
    assertEquals(1, capturedProducts.size(),
        "Product list size should be 1");
    assertEquals(1, capturedProducts.get(0).getCustomOrder(),
        "Product list custom order should be 1");
  }

  @Test
  void testGetCustomSortProducts() {
    ProductMarketplaceData productA = getMockProductMarketplaceData();
    ProductMarketplaceData productB = getMockProductMarketplaceData2();

    when(productMarketplaceDataRepo.findByCustomOrderIsNotNullOrderByCustomOrderDesc())
        .thenReturn(List.of(productA, productB));
    when(productCustomSortRepo.findAll())
        .thenReturn(List.of(new ProductCustomSort(SortOption.STANDARD.getOption())));

    ProductCustomSortRequest result = productMarketplaceDataService.getCustomSortProducts();

    assertEquals(List.of(MOCK_PRODUCT_ID, SAMPLE_PRODUCT_ID), result.getOrderedListOfProducts(),
        "Ordered products should mirror repository order");
    assertEquals(SortOption.STANDARD.getOption(), result.getRuleForRemainder(),
        "Remainder rule should come from stored configuration");
  }

  @Test
  void testGetCustomSortProductsDefaultsWhenNoConfig() {
    when(productMarketplaceDataRepo.findByCustomOrderIsNotNullOrderByCustomOrderDesc()).thenReturn(List.of());
    when(productCustomSortRepo.findAll()).thenReturn(List.of());

    ProductCustomSortRequest result = productMarketplaceDataService.getCustomSortProducts();

    assertTrue(result.getOrderedListOfProducts().isEmpty(), "Ordered products should be empty when repository has none");
    assertEquals(SortOption.ALPHABETICALLY.getOption(), result.getRuleForRemainder(),
        "Default remainder rule should be alphabetically");
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort() throws InvalidParamException {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));

    List<ProductMarketplaceData> refinedProducts =
        productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts);

    assertEquals(1, refinedProducts.size(),
        "Product list size should be 1");
    assertEquals(1, refinedProducts.get(0).getCustomOrder(),
        "Product list custom order should be 1");
    verify(productMarketplaceDataRepo).findById(SAMPLE_PRODUCT_ID);
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSortProductNotFound() {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(
        NotFoundException.class,
        () -> productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts),
        "Expected NotFoundException when product is not found"
    );

    assertEquals(
        ErrorCode.PRODUCT_NOT_FOUND.getCode(),
        exception.getCode(),
        "Exception code should indicate PRODUCT_NOT_FOUND"
    );
  }

  @Test
  void testUpdateProductInstallationCountWhenNotSynchronized() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(false);
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    when(productMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(productMarketplaceDataRepo.updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt())).thenReturn(10);

    int result = productMarketplaceDataService.updateProductInstallationCount(SAMPLE_PRODUCT_ID);

    assertEquals(10, result,
        "Installation count should match 10 when not synchronized");
    verify(productMarketplaceDataRepo).updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt());
  }

  @Test
  void testUpdateInstallationCountForProduct() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(true);
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(new Product()));
    when(productMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(productMarketplaceDataRepo.increaseInstallationCount(SAMPLE_PRODUCT_ID)).thenReturn(4);

    int result = productMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(4, result, "Installation count should match 4");

    result = productMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID, StringUtils.EMPTY);
    assertEquals(4, result, "Installation count should match 4");
  }

  @Test
  void testSyncInstallationCountWithNewProduct() {
    ProductMarketplaceData mockProductMarketplaceData = ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).build();
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    int installationCount = productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertTrue(installationCount >= 20 && installationCount <= 50,
        "Installation count should be more than 20 and less than 50");
  }

  @Test
  void testGetInstallationCountFromFileOrInitializeRandomly() {
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();

    int installationCount = productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertEquals(40, installationCount,
        "Installation count should match 40 from file");
  }

  @Test
  void testBuildArtifactStreamFromResource() {
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));
    OutputStream result = productMarketplaceDataService.buildArtifactStreamFromResource(MOCK_DOWNLOAD_URL,
        getMockResource(), new ByteArrayOutputStream());
    assertNotNull(result, "Artifact stream should not be null with existed artifact");
  }

  @Test
  void testGetProductArtifactStreamShouldReturnResource() {
    MavenArtifactVersion mav = new MavenArtifactVersion();
    mav.setDownloadUrl(MOCK_DOWNLOAD_URL);
    when(mavenArtifactVersionRepo.findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION))
        .thenReturn(List.of(mav));
    ByteArrayResource resource = new ByteArrayResource("data".getBytes());
    ResponseEntity<Resource> responseEntity = ResponseEntity.ok(resource);
    when(fileDownloadService.fetchUrlResource(MOCK_DOWNLOAD_URL)).thenReturn(responseEntity);

    ResponseEntity<Resource> result = productMarketplaceDataService.getProductArtifactStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION);

    assertNotNull(result, "Result stream should not be null with existed artifact");
    assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode(), "Response entity should return code of 200");
    assertEquals(result.getBody(), resource, "Response's body should equal to the content form received stream");
    verify(mavenArtifactVersionRepo).findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION);
    verify(fileDownloadService).fetchUrlResource(MOCK_DOWNLOAD_URL);
  }

  @Test
  void testUpdateSuccessorForProductWhenProductNotFound() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.empty());
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNotNull(response, "Response should not be null even when product is not found");
    assertNull(response.getPullRequestUrl(), "Pull request URL should be null when product is not found");
    verify(productMarketplaceDataRepo).save(any(ProductMarketplaceData.class));
    verify(productRepo, never()).save(any(Product.class));
    verify(gitHubService, never()).modifyReadmeUnsupportedPullRequest(anyString(), any());
  }

  @Test
  void testUpdateSuccessorForProductSavesMarketplaceDataWithCorrectValues() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);
    request.setDeprecationRequester("admin");

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.empty());
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    productMarketplaceDataService.updateSuccessorForProduct(request);

    ArgumentCaptor<ProductMarketplaceData> captor = ArgumentCaptor.forClass(ProductMarketplaceData.class);
    verify(productMarketplaceDataRepo).save(captor.capture());
    ProductMarketplaceData saved = captor.getValue();
    assertEquals("https://successor.com", saved.getSuccessor(), "Successor URL should be persisted");
    assertEquals("admin", saved.getDeprecationRequester(), "Deprecation requester should be persisted");
    assertNotNull(saved.getDeprecationDate(), "Deprecation date should be set automatically");
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeFalseDoesNotCreatePr() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);
    Product product = getMockProduct();

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNull(response.getPullRequestUrl(), "Pull request URL should be null when addReadme is false");
    assertEquals(Boolean.TRUE, product.getDeprecated(), "Product deprecated flag should be set to true");
    verify(productRepo).save(product);
    verify(gitHubService, never()).modifyReadmeUnsupportedPullRequest(anyString(), any());
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeTrueAndNullActionDoesNotCreatePr() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, true, null);
    Product product = getMockProduct();

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNull(response.getPullRequestUrl(), "Pull request URL should be null when pullRequestAction is null");
    verify(gitHubService, never()).modifyReadmeUnsupportedPullRequest(anyString(), any());
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeTrueAndActionAddReturnsPrUrl() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, true, PullRequestAction.ADD);
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);

    GHPullRequest mockPr = mock(GHPullRequest.class);
    when(mockPr.getHtmlUrl()).thenReturn(URI.create("https://github.com/axonivy-market/bpmn-statistic/pull/1").toURL());

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(gitHubService.modifyReadmeUnsupportedPullRequest(MOCK_PRODUCT_REPOSITORY_NAME, PullRequestAction.ADD)).thenReturn(mockPr);
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNotNull(response.getPullRequestUrl(), "Pull request URL should be present when GitHub service returns a PR");
    assertEquals("https://github.com/axonivy-market/bpmn-statistic/pull/1", response.getPullRequestUrl(),
        "Pull request URL should match the mocked GitHub PR HTML URL");
    verify(gitHubService).modifyReadmeUnsupportedPullRequest(MOCK_PRODUCT_REPOSITORY_NAME, PullRequestAction.ADD);
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeTrueAndActionRemoveReturnsPrUrl() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest(null, false, true, PullRequestAction.REMOVE);
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);

    GHPullRequest mockPr = mock(GHPullRequest.class);
    when(mockPr.getHtmlUrl()).thenReturn(URI.create("https://github.com/axonivy-market/bpmn-statistic/pull/2").toURL());

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(gitHubService.modifyReadmeUnsupportedPullRequest(MOCK_PRODUCT_REPOSITORY_NAME, PullRequestAction.REMOVE)).thenReturn(mockPr);
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNotNull(response.getPullRequestUrl(), "Pull request URL should be present for REMOVE action");
    verify(gitHubService).modifyReadmeUnsupportedPullRequest(MOCK_PRODUCT_REPOSITORY_NAME, PullRequestAction.REMOVE);
  }

  @Test
  void testUpdateSuccessorForProductWhenGitHubReturnsNullPrSetsPullRequestUrlToNull() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, true, PullRequestAction.ADD);
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(gitHubService.modifyReadmeUnsupportedPullRequest(MOCK_PRODUCT_REPOSITORY_NAME, PullRequestAction.ADD)).thenReturn(null);
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of());

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNull(response.getPullRequestUrl(), "Pull request URL should be null when GitHub service returns null");
  }

  @Test
  void testUpdateSuccessorForProductReturnsDeprecatedProductList() throws IOException {
    DeprecatedRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);

    ProductDeprecationProjection projection = mock(ProductDeprecationProjection.class);
    when(projection.getId()).thenReturn(MOCK_PRODUCT_ID);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.empty());
    when(productRepo.findProductIdsByDeprecated(true)).thenReturn(List.of(projection));

    DeprecatedResponse response = productMarketplaceDataService.updateSuccessorForProduct(request);

    assertNotNull(response.getProductDeprecations(), "Product deprecations list should not be null");
    assertEquals(1, response.getProductDeprecations().size(), "Should return all deprecated products from repository");
    assertEquals(MOCK_PRODUCT_ID, response.getProductDeprecations().getFirst().getId(),
        "Returned deprecated product ID should match");
  }

  private DeprecatedRequest buildDeprecatedRequest(
      String successorUrl, Boolean deprecated, boolean addReadme, PullRequestAction action) {
    DeprecatedRequest request = new DeprecatedRequest();
    request.setProductId(BaseSetup.MOCK_PRODUCT_ID);
    request.setSuccessorUrl(successorUrl);
    request.setDeprecated(deprecated);
    request.setAddReadme(addReadme);
    request.setPullRequestAction(action);
    return request;
  }
}