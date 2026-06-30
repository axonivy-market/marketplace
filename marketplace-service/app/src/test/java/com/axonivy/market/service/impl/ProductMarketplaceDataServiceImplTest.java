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
import com.axonivy.market.enums.RepositoryAction;
import com.axonivy.market.exceptions.model.ArchiveNotAllowedException;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.DeprecationRequest;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.rest.axonivy.AxonIvyClient;
import com.axonivy.market.service.FileDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHPullRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
  @Mock
  private AxonIvyClient axonIvyClient;
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
    assertEquals(1, capturedProducts.getFirst().getCustomOrder(),
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

    assertTrue(result.getOrderedListOfProducts().isEmpty(),
        "Ordered products should be empty when repository has none");
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
    assertEquals(1, refinedProducts.getFirst().getCustomOrder(),
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
    when(productMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(productMarketplaceDataRepo.updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt())).thenReturn(10);

    ProductMarketplaceData result = productMarketplaceDataService.updateProductInstallationCount(SAMPLE_PRODUCT_ID);

    assertEquals(10, result.getInstallationCount(),
        "Installation count should match 10 when not synchronized");
    verify(productMarketplaceDataRepo).updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt());
  }

  @Test
  void testUpdateInstallationCountForProduct() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(true);
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
  void testBuildArtifactStreamFromResource() {
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));
    OutputStream result = productMarketplaceDataService.buildArtifactStreamFromResource(MOCK_DOWNLOAD_URL,
        getMockResource(), new ByteArrayOutputStream());
    assertNotNull(result, "Artifact stream should not be null with existed artifact");
  }

  @Test
  void testGetProductArtifactStreamShouldReturnResource() {
    MavenArtifactVersion mav = new MavenArtifactVersion();
    mav.setDownloadUrl(MOCK_DOWNLOAD_URL);
    when(mavenArtifactVersionRepo.findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID,
        MOCK_RELEASED_VERSION))
        .thenReturn(List.of(mav));
    ByteArrayResource resource = new ByteArrayResource("data".getBytes());
    ResponseEntity<Resource> responseEntity = ResponseEntity.ok(resource);
    when(fileDownloadService.fetchUrlResource(MOCK_DOWNLOAD_URL)).thenReturn(responseEntity);

    ResponseEntity<Resource> result = productMarketplaceDataService.getProductArtifactStream(MOCK_PRODUCT_ID,
        MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION);

    assertNotNull(result, "Result stream should not be null with existed artifact");
    assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode(), "Response entity should return code of 200");
    assertEquals(result.getBody(), resource, "Response's body should equal to the content form received stream");
    verify(mavenArtifactVersionRepo).findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID,
        MOCK_RELEASED_VERSION);
    verify(fileDownloadService).fetchUrlResource(MOCK_DOWNLOAD_URL);
  }

  @Test
  void testUpdateSuccessorForProductWhenProductNotFound() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData
        ()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.empty());

    String response = productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    assertNull(response, "Pull request URL should be null when product is not found");
    verify(productMarketplaceDataRepo).save(any(ProductMarketplaceData.class));
    verify(productRepo, never()).save(any(Product.class));
    verify(gitHubService, never()).updateReadmeForSuccessorNotes(anyString(), any(), any());
  }

  @Test
  void testUpdateSuccessorForProductSavesMarketplaceDataWithCorrectValues() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);
    request.setDeprecationRequester("admin");

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.empty());

    productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    ArgumentCaptor<ProductMarketplaceData> captor = ArgumentCaptor.forClass(ProductMarketplaceData.class);
    verify(productMarketplaceDataRepo).save(captor.capture());
    ProductMarketplaceData saved = captor.getValue();
    assertEquals("https://successor.com", saved.getSuccessor(), "Successor URL should be persisted");
    assertEquals("admin", saved.getDeprecationRequester(), "Deprecation requester should be persisted");
    assertNotNull(saved.getDeprecationDate(), "Deprecation date should be set automatically");
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeFalseDoesNotCreatePr() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest("https://successor.com", true, false, null);
    Product product = getMockProduct();
    product.setNewestReleaseVersion(MOCK_RELEASED_VERSION);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData
        ()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(axonIvyClient.getAllVersions()).thenReturn(List.of());

    String response = productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    assertNull(response, "Pull request URL should be null when addReadme is false");
    assertEquals(Boolean.TRUE, product.getDeprecated(), "Product deprecated flag should be set to true");
    verify(productRepo).save(product);
    verify(gitHubService, never()).updateReadmeForSuccessorNotes(anyString(), any(), any());
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeTrueAndNullActionDoesNotCreatePr() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest("https://successor.com", true, true, null);
    Product product = getMockProduct();
    product.setNewestReleaseVersion(MOCK_RELEASED_VERSION);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(
        Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(axonIvyClient.getAllVersions()).thenReturn(List.of());

    String response = productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    assertNull(response, "Pull request URL should be null when pullRequestAction is null");
    verify(gitHubService, never()).updateReadmeForSuccessorNotes(anyString(), any(), any());
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeTrueAndActionAddReturnsPrUrl() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest("https://successor.com", true, true, PullRequestAction.ADD);
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
    product.setNewestReleaseVersion(MOCK_RELEASED_VERSION);

    GHPullRequest mockPr = mock(GHPullRequest.class);
    when(mockPr.getHtmlUrl()).thenReturn(URI.create("https://github.com/axonivy-market/bpmn-statistic/pull/1").toURL());

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(axonIvyClient.getAllVersions()).thenReturn(List.of());
    when(gitHubService.updateReadmeForSuccessorNotes(eq(MOCK_PRODUCT_REPOSITORY_NAME), eq(PullRequestAction.ADD), any())).thenReturn(
        mockPr);

    String response = productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    assertNotNull(response, "Pull request URL should be present when GitHub service returns a PR");
    assertEquals("https://github.com/axonivy-market/bpmn-statistic/pull/1", response,
        "Pull request URL should match the mocked GitHub PR HTML URL");
    verify(gitHubService).updateReadmeForSuccessorNotes(eq(MOCK_PRODUCT_REPOSITORY_NAME), eq(PullRequestAction.ADD), any());
  }

  @Test
  void testUpdateSuccessorForProductWithAddReadmeTrueAndActionRemoveReturnsPrUrl() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest(null, false, true, PullRequestAction.REMOVE);
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
    product.setNewestReleaseVersion(MOCK_RELEASED_VERSION);

    GHPullRequest mockPr = mock(GHPullRequest.class);
    when(mockPr.getHtmlUrl()).thenReturn(URI.create("https://github.com/axonivy-market/bpmn-statistic/pull/2").toURL());

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(axonIvyClient.getAllVersions()).thenReturn(List.of());
    when(gitHubService.updateReadmeForSuccessorNotes(eq(MOCK_PRODUCT_REPOSITORY_NAME), eq(PullRequestAction.REMOVE), any()))
        .thenReturn(mockPr);

    String response = productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    assertNotNull(response, "Pull request URL should be present for REMOVE action");
    verify(gitHubService).updateReadmeForSuccessorNotes(eq(MOCK_PRODUCT_REPOSITORY_NAME), eq(PullRequestAction.REMOVE), any());
  }

  @Test
  void testUpdateSuccessorForProductWhenGitHubReturnsNullPrSetsPullRequestUrlToNull() throws IOException {
    DeprecationRequest request = buildDeprecatedRequest("https://successor.com", true, true, PullRequestAction.ADD);
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
    product.setNewestReleaseVersion(MOCK_RELEASED_VERSION);

    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(getMockProductMarketplaceData()));
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(axonIvyClient.getAllVersions()).thenReturn(List.of());
    when(gitHubService.updateReadmeForSuccessorNotes(eq(MOCK_PRODUCT_REPOSITORY_NAME), eq(PullRequestAction.ADD), any()))
        .thenReturn(null);
    String response = productMarketplaceDataService.updateSuccessorForProduct(MOCK_PRODUCT_ID, request);

    assertNull(response, "Pull request URL should be null when GitHub service returns null");
  }

  @Test
  void testArchiveOrUnarchiveRepositoryWhenProductNotFoundShouldThrowNotFoundException() {
    when(productRepo.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> productMarketplaceDataService.archiveOrUnarchiveRepository("non-existent", RepositoryAction.ARCHIVE),
        "Expected NotFoundException when product is not found");
  }

  @Test
  void testArchiveOrUnarchiveRepositoryWhenUnarchiveShouldCallUnarchiveAndSave() throws IOException {
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));

    productMarketplaceDataService.archiveOrUnarchiveRepository(MOCK_PRODUCT_ID, RepositoryAction.UNARCHIVE);

    verify(gitHubService).unArchivedTheRepository(MOCK_PRODUCT_REPOSITORY_NAME);
    verify(gitHubService, never()).archiveTheRepository(anyString());
    assertNull(ReflectionTestUtils.getField(product, "isArchived"), "isArchived should be null after unarchive");
    verify(productRepo).save(product);
  }

  @Test
  void testArchiveOrUnarchiveRepositoryWhenArchiveWithDeprecationWarningShouldArchiveAndSave() throws IOException {
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(gitHubService.hasDeprecationWarningInReadme(MOCK_PRODUCT_REPOSITORY_NAME)).thenReturn(true);

    productMarketplaceDataService.archiveOrUnarchiveRepository(MOCK_PRODUCT_ID, RepositoryAction.ARCHIVE);

    verify(gitHubService).hasDeprecationWarningInReadme(MOCK_PRODUCT_REPOSITORY_NAME);
    verify(gitHubService).archiveTheRepository(MOCK_PRODUCT_REPOSITORY_NAME);
    assertTrue(Boolean.TRUE.equals(ReflectionTestUtils.getField(product, "isArchived")),
        "isArchived should be true after archive");
    verify(productRepo).save(product);
  }

  @Test
  void testArchiveOrUnarchiveRepositoryWhenArchiveWithoutDeprecationWarningShouldThrowArchiveNotAllowed()
      throws IOException {
    Product product = getMockProduct();
    product.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
    when(productRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(product));
    when(gitHubService.hasDeprecationWarningInReadme(MOCK_PRODUCT_REPOSITORY_NAME)).thenReturn(false);

    assertThrows(ArchiveNotAllowedException.class,
        () -> productMarketplaceDataService.archiveOrUnarchiveRepository(MOCK_PRODUCT_ID, RepositoryAction.ARCHIVE),
        "Expected ArchiveNotAllowedException when README has no deprecation warning");

    verify(gitHubService, never()).archiveTheRepository(anyString());
    verify(productRepo, never()).save(any());
  }

  private DeprecationRequest buildDeprecatedRequest(
      String successorUrl, Boolean deprecated, boolean addReadme, PullRequestAction action) {
    DeprecationRequest request = new DeprecationRequest();
    request.setSuccessorUrl(successorUrl);
    request.setIsDeprecated(deprecated);
    request.setIsAddReadme(addReadme);
    request.setPullRequestAction(action);
    return request;
  }
}

