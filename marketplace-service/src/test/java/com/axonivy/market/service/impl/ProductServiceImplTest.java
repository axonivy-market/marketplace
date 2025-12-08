package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.repository.*;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.HttpFetchingUtils;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHTag;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;
import static com.axonivy.market.enums.DocumentField.SHORT_DESCRIPTIONS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseSetup {
  private static final long LAST_CHANGE_TIME = 1718096290000L;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  private static final String EMPTY_SOURCE_URL_META_JSON_FILE = "/emptySourceUrlMeta.json";
  private static final String META_JSON_FILE_WITH_VENDOR_INFORMATION = "/meta-with-vendor-information.json";
  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
  @Captor
  ArgumentCaptor<ArrayList<ProductModuleContent>> argumentCaptorProductModuleContents;
  @Captor
  ArgumentCaptor<ProductModuleContent> argumentCaptorProductModuleContent;
  @Captor
  ArgumentCaptor<ProductSearchCriteria> productSearchCriteriaArgumentCaptor;
  @Captor
  ArgumentCaptor<ProductMarketplaceData> argumentCaptorProductMarketplaceData;
  private String keyword;
  private String language;
  private Page<Product> mockResultReturn;
  @Mock
  private ProductRepository productRepo;
  @Mock
  private ProductModuleContentRepository productModuleContentRepo;
  @Mock
  private ProductJsonContentRepository productJsonContentRepo;
  @Mock
  private GHAxonIvyMarketRepoService marketRepoService;
  @Mock
  private GitHubRepoMetaRepository repoMetaRepo;
  @Mock
  private GitHubService gitHubService;
  @Mock
  private MetadataService metadataService;
  @Mock
  private ImageRepository imageRepo;
  @Mock
  private MetadataRepository metadataRepo;
  @Mock
  private ImageService imageService;
  @Mock
  private ExternalDocumentService externalDocumentService;
  @Mock
  private ProductContentService productContentService;
  @Mock
  private GHAxonIvyProductRepoService axonIvyProductRepoService;
  @Mock
  private ProductMarketplaceDataService productMarketplaceDataService;
  @Mock
  private ProductMarketplaceDataRepository productMarketplaceDataRepo;
  @Mock
  private VersionService versionService;
  @Mock
  private ArtifactRepository artifactRepo;
  @Mock
  private GithubRepoRepository githubRepoRepository;
  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepository;
  @Mock
  private FileDownloadService fileDownloadService;
  @Spy
  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindProducts() {
    language = "en";
    // Start testing by All
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result,
        "All products should match mock result");

    // Start testing by Connector
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result,
        "Connector type products should match mock result");

    // Start testing by Other
    // Executes
    result = productService.findProducts(TypeOption.DEMOS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(2, result.getSize(),
        "Number of demo type products should match number of mock demo type result");
  }

  @Test
  void testFindProductsInRESTClientOfDesigner() {
    productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, Language.EN.getValue(), true, PAGEABLE);
    verify(productRepo).searchByCriteria(productSearchCriteriaArgumentCaptor.capture(), any(Pageable.class));
    assertEquals(List.of(SHORT_DESCRIPTIONS), productSearchCriteriaArgumentCaptor.getValue().getExcludeFields(),
        "Product short descriptions should match input short descriptions");
  }

  // Using a random UUID in test; no dedicated constant/ID needed
  @SuppressWarnings("java:S5977")
  @Test
  void testSyncProductsAsUpdateMetaJSONFromGitHub() throws IOException {
    // Start testing by adding new meta
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var mockGithubFile = new GitHubFile();
    mockGithubFile.setFileName(META_FILE);
    mockGithubFile.setType(FileType.META);
    mockGithubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
    var mockGHContent = mockGHContentAsMetaJSON();
    when(gitHubService.getGHContent(any(), anyString(), any())).thenReturn(mockGHContent);
    when(mockGHContent.read()).thenReturn(this.getClass().getResourceAsStream(EMPTY_SOURCE_URL_META_JSON_FILE));
    when(productRepo.save(any(Product.class))).thenReturn(new Product());

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");

    // Start testing by deleting new meta
    mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    mockGithubFile.setStatus(FileStatus.REMOVED);
    // Executes
    result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");
  }

  @Test
  void testSyncProductsAsNewMetaJSONFromGitHub() throws IOException {
    // Start testing by adding new meta
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var mockGithubFile = new GitHubFile();
    mockGithubFile.setFileName("connector/" + META_FILE);
    mockGithubFile.setType(FileType.META);
    mockGithubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
    var mockGHContent = mockGHContentAsMetaJSON();
    when(gitHubService.getGHContent(any(), anyString(), any())).thenReturn(mockGHContent);
    when(mockGHContent.read()).thenReturn(this.getClass().getResourceAsStream(META_JSON_FILE_WITH_VENDOR_INFORMATION));
    when(productRepo.save(any(Product.class))).thenReturn(new Product());
    var mockGhTag = mock(GHTag.class);
    when(mockGhTag.getCommit()).thenReturn(mockCommit);
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(gitHubService.getRepositoryTags("axonivy-market/jira-connector")).thenReturn(List.of(mockGhTag));

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");

    // Start testing by deleting new meta
    mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    var mockDate = new Date();
    when(mockCommit.getCommitDate()).thenReturn(mockDate);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    mockGithubFile.setStatus(FileStatus.REMOVED);
    // Executes
    result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");
  }

  // Using a random UUID in test; no dedicated constant/ID needed
  @SuppressWarnings("java:S5977")
  @Test
  void testSyncProductsAsUpdateLogoFromGitHub() throws IOException {
    // Start testing by adding new logo
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var mockGitHubFile = new GitHubFile();
    mockGitHubFile.setFileName(LOGO_FILE);
    mockGitHubFile.setType(FileType.LOGO);
    mockGitHubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));

    // Executes
    result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");
  }

  @Test
  void testFindAllProductsWithKeyword() {
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "All products should match mock result");
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));

    // Test has keyword
    when(productRepo.searchByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME))
            .toList()));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
    assertTrue(result.hasContent(), "Result product list should not be empty");
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()),
        "First product should match mock product name");

    // Test has keyword and type is connector
    when(productRepo.searchByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME)
                && product.getType().equals(TypeOption.CONNECTORS.getCode()))
            .toList()));
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, language, false,
        PAGEABLE);
    assertTrue(result.hasContent(), "Result connector type product list should not be empty");
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()),
        "First product should match mock product name");
  }

  @Test
  void testSyncProductsFirstTime() throws IOException {
    var mockCommit = mockGHCommitHasSHA1WithCommitDate(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(null);
    when(productContentService.getReadmeAndProductContentsFromVersion(any(), anyString(), anyString(),
        any(), anyString())).thenReturn(mockReadmeProductContent());

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, mockMetaJsonAndLogoList());
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
    when(productModuleContentRepo.saveAll(anyList())).thenReturn(List.of(mockReadmeProductContent()));

    when(imageService.mappingImageFromGHContent(any(), any())).thenReturn(getMockImage());
    when(productRepo.save(any(Product.class))).thenReturn(new Product());
    when(fileDownloadService.getFileAsString(anyString())).thenReturn(getMockMetadataContent3());
    // Executes
    productService.syncLatestDataFromMarketRepo(false);

    verify(productModuleContentRepo).saveAll(argumentCaptorProductModuleContents.capture());
    verify(productRepo).save(argumentCaptor.capture());
    assertEquals(argumentCaptorProductModuleContents.getValue().get(0).getId(), mockReadmeProductContent().getId(),
        "Product module contents ID should match mock readme product module content ID");
  }

  @Test
  void testSyncProductsFirstTimeWithOutSourceUrl() throws IOException {
    var mockCommit = mockGHCommitHasSHA1WithCommitDate(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(null);

    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(EMPTY_SOURCE_URL_META_JSON_FILE);
    when(mockContent.read()).thenReturn(inputStream);

    var mockContentLogo = mockGHContentAsLogo();

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    List<GHContent> mockMetaJsonAndLogoList = new ArrayList<>(List.of(mockContent, mockContentLogo));
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, mockMetaJsonAndLogoList);
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
    when(imageService.mappingImageFromGHContent(any(), any())).thenReturn(getMockImage());
    when(productRepo.save(any(Product.class))).thenReturn(new Product());
    // Executes
    productService.syncLatestDataFromMarketRepo(false);
    verify(productModuleContentRepo).save(argumentCaptorProductModuleContent.capture());
    assertEquals("1.0", argumentCaptorProductModuleContent.getValue().getVersion(),
        "Product module content version should be 1.0");
  }

  @Test
  void testSyncProductsSecondTime() {
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class);
         MockedStatic<HttpFetchingUtils> mockHttpUtils = Mockito.mockStatic(HttpFetchingUtils.class)) {
      Product mockProduct = getMockProduct();
      mockProduct.setProductModuleContent(mockReadmeProductContent());
      mockProduct.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
      mockForSyncSecondTime(mockProduct);
      mockHttpUtils.when(() -> HttpFetchingUtils.getFileAsString(any())).thenReturn(getMockMetadataContent());
      when(fileDownloadService.getFileAsString(any())).thenReturn(getMockMetadataContent2());
      // Executes
      productService.syncLatestDataFromMarketRepo(false);

      verify(productModuleContentRepo).saveAll(argumentCaptorProductModuleContents.capture());
      verify(productRepo).save(argumentCaptor.capture());
      assertEquals(argumentCaptorProductModuleContents.getValue().get(0).getId(), mockReadmeProductContent().getId(),
          "Product module contents ID should match mock readme product module content ID");
    }
  }

  @Test
  void testSyncProductsSecondTimeAndThereIsNoDuplicatedValueInReleasedVersion() {
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class);
         MockedStatic<HttpFetchingUtils> mockHttpUtils = Mockito.mockStatic(HttpFetchingUtils.class)) {
      List<String> mockVersions = Arrays.asList("10.0.10", "10.0.10-SNAPSHOT", "10.0.10-m123", "10.0.11-SNAPSHOT",
          "10.0.12-SNAPSHOT", "10.0.13-SNAPSHOT");
      Product mockProduct = getMockProduct();
      mockProduct.getReleasedVersions().add("10.0.10-SNAPSHOT");
      mockProduct.setProductModuleContent(mockReadmeProductContent());
      mockProduct.setRepositoryName(MOCK_PRODUCT_REPOSITORY_NAME);
      mockForSyncSecondTime(mockProduct);
      mockHttpUtils.when(() -> HttpFetchingUtils.getFileAsString(any())).thenReturn(getMockMetadataContent2());
      when(fileDownloadService.getFileAsString(any())).thenReturn(getMockMetadataContent2());
      // Executes
      productService.syncLatestDataFromMarketRepo(false);
      verify(productRepo).save(argumentCaptor.capture());
      assertEquals(argumentCaptor.getValue().getReleasedVersions(), mockVersions,
          "Product released versions should match mock readme product versions");
    }
  }

  private void mockForSyncSecondTime(Product mockProduct) {
    HashMap<String, String> names = new HashMap<>();
    names.put(ProductJsonConstants.EN_LANGUAGE, MOCK_PRODUCT_NAME);
    mockProduct.setNames(names);
    var gitHubRepoMeta = mock(GitHubRepoMeta.class);
    when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

    when(productRepo.findAllProductsWithNamesAndShortDescriptions()).thenReturn(List.of(mockProduct));

    ProductModuleContent mockReturnProductContent = mockReadmeProductContent();
    mockReturnProductContent.setVersion(MOCK_RELEASED_VERSION);

    when(productContentService.getReadmeAndProductContentsFromVersion(any(), anyString(), anyString(), any(),
        anyString())).thenReturn(mockReturnProductContent);
    when(productModuleContentRepo.saveAll(anyList()))
        .thenReturn(List.of(mockReadmeProductContent(), mockReturnProductContent));

    when(MavenUtils.buildDownloadUrl(any(), any(), any(), any(), any(), any())).thenReturn(MOCK_DOWNLOAD_URL);
  }

  @Test
  void testNothingToSync() {
    var gitHubRepoMeta = mock(GitHubRepoMeta.class);
    when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data list from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data list from Market repo should be empty if there is nothing to sync");
  }

  @Test
  void testSyncNullProductModuleContent() throws IOException {
    var mockCommit = mockGHCommitHasSHA1WithCommitDate(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(null);

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, new ArrayList<>());
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
    when(productRepo.save(any(Product.class))).thenReturn(new Product());
    when(productRepo.findAllProductsWithNamesAndShortDescriptions()).thenReturn(List.of(getMockProduct()));

    // Executes
    productService.syncLatestDataFromMarketRepo(false);
    verify(productRepo).save(argumentCaptor.capture());

    assertNull(argumentCaptor.getValue().getProductModuleContent(),
        "Product module content should be null");
  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = TypeOption.ALL.getOption();
    keyword = "on";
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(
        mockResultReturn);

    var result = productService.findProducts(type, keyword, language, false, simplePageable);
    assertEquals(result, mockResultReturn, "Product list from search query should match mock product list");
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));
  }

  @Test
  void testFetchProductDetail() {
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersionWithData();
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(mockMavenArtifactVersion);
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(null);
    Product result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertNull(result, "Product should be null");
  }

  @Test
  void testGetCompatibilityRangeAfterFetchProductDetail() {
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersionWithData();
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(mockMavenArtifactVersion);
    Product product = getMockProduct();
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(product);
    when(versionService.getInstallableVersions(MOCK_PRODUCT_ID, false, null))
        .thenReturn(mockVersionAndUrlModels(), mockVersionModels(), mockVersionModels2(), mockVersionModels3());

    Product result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertEquals("10.0+", result.getCompatibilityRange(),
        "Product compatibility range should match 10.0+");

    result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertEquals("10.0 - 11.3+", result.getCompatibilityRange(),
        "Product compatibility range should match 10.0 - 11.3+");

    product.setDeprecated(true);
    result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertEquals("10.0 - 11.3", result.getCompatibilityRange(),
        "Product compatibility range should match 10.0 - 11.3");

    product.setDeprecated(false);
    result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertEquals("10.0 - 12.0+", result.getCompatibilityRange(),
        "Product compatibility range should match 10.0 - 12.0");

    product.setDeprecated(true);
    result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertEquals("10.0 - 12.0", result.getCompatibilityRange(),
        "Product compatibility range should match 10.0 - 12.0");
    verify(versionService, atLeastOnce()).getInstallableVersions(MOCK_PRODUCT_ID, false, null);
    verify(versionService, never()).getInstallableVersions(MOCK_PRODUCT_ID, true, null);
  }

  @Test
  void testGetProductByIdWithNewestReleaseVersion() {
    List<MavenArtifactVersion> mockMavenArtifactVersions = getMockMavenArtifactVersionWithData();
    Product mockProduct = getMockProduct();

    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class);
         MockedStatic<VersionUtils> mockVersionUtils = Mockito.mockStatic(VersionUtils.class)) {
      mockUtils.when(() -> mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(
          mockMavenArtifactVersions);
      when(VersionUtils.extractAllVersions(mockMavenArtifactVersions, true))
          .thenReturn(List.of(MOCK_SNAPSHOT_VERSION));

      when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);
      when(productJsonContentRepo.findByProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION))
          .thenReturn(List.of(getMockProductJsonContentContainMavenDropins()));

      Product result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
      assertEquals(mockProduct, result,
          "Product with newest release version should match mock product");

      when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());
      when(productRepo.getReleasedVersionsById(MOCK_PRODUCT_ID)).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
      when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);
      when(VersionUtils.getVersionsToDisplay(any(), any())).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
      result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
      assertEquals(mockProduct, result,
          "Product with newest release version should match mock product");
    }
  }

  @Test
  void testGetProductByIdWithNewestReleaseVersionWithEmptyArtifact() {
    Product mockProduct = getMockProduct();
    when(productJsonContentRepo.findByProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION))
        .thenReturn(List.of(getMockProductJsonContentContainMavenDropins()));
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());
    when(productRepo.getReleasedVersionsById(MOCK_PRODUCT_ID)).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);

    Product result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
    assertEquals(mockProduct, result,
        "Product with newest release version and empty artifact should match mock product");
  }

  @Test
  void testFetchProductDetailByIdAndVersion() {
    GithubRepo mockGithubRepo = new GithubRepo();
    mockGithubRepo.setName(MOCK_PRODUCT_REPOSITORY_NAME);
    mockGithubRepo.setFocused(true);
    Product mockProduct = mockResultReturn.getContent().get(0);
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(mockProduct);
    Product result = productService.fetchProductDetailByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(mockProduct, result, "Product detail by id and version should match mock product");
    verify(productRepo).getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testFetchBestMatchProductDetailByIdAndVersion() {
    Product mockProduct = getMockProduct();
    Metadata mockMetadata = getMockMetadataWithVersions();
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockMetadata.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    when(metadataRepo.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockMetadata));
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(mockProduct);
    when(productMarketplaceDataService.updateProductInstallationCount(MOCK_PRODUCT_ID)).thenReturn(
        mockProductMarketplaceData.getInstallationCount());
    Product result = productService.fetchBestMatchProductDetail(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, result,
        "Found best match product version should match mock product");
  }

  private void mockMarketRepoMetaStatus() {
    var mockMarketRepoMeta = new GitHubRepoMeta();
    mockMarketRepoMeta.setRepoURL(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMarketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMarketRepoMeta.setLastChange(LAST_CHANGE_TIME);
    mockMarketRepoMeta.setLastSHA1(SHA1_SAMPLE);
    when(repoMetaRepo.findByRepoName(any())).thenReturn(mockMarketRepoMeta);
  }

  private GHCommit mockGHCommitHasSHA1(String sha1) {
    var mockCommit = mock(GHCommit.class);
    when(mockCommit.getSHA1()).thenReturn(sha1);
    return mockCommit;
  }

  private GHCommit mockGHCommitHasSHA1WithCommitDate(String sha1) throws IOException {
    var mockCommit = mock(GHCommit.class);
    when(mockCommit.getSHA1()).thenReturn(sha1);
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    return mockCommit;
  }

  private GHContent mockGHContentAsMetaJSON() {
    var mockGHContent = mock(GHContent.class);
    when(mockGHContent.getName()).thenReturn(META_FILE);
    return mockGHContent;
  }

  private GHContent mockGHContentAsLogo() {
    var mockGHContent = mock(GHContent.class);
    when(mockGHContent.getName()).thenReturn("logo.png");
    return mockGHContent;
  }

  private ProductModuleContent mockReadmeProductContent() {
    ProductModuleContent productModuleContent = new ProductModuleContent();
    productModuleContent.setId(MOCK_PRODUCT_ID_WITH_VERSION);
    productModuleContent.setVersion(MOCK_RELEASED_VERSION);
    productModuleContent.setName(MOCK_PRODUCT_NAME);
    Map<String, String> description = new HashMap<>();
    description.put(Language.EN.getValue(), "testDescription");
    productModuleContent.setDescription(description);
    return productModuleContent;
  }

  // Using a random UUID in test; no dedicated constant/ID needed
  @SuppressWarnings("java:S5977")
  @Test
  void testUpdateNewLogoFromGitHubRemoveOldLogo() throws IOException {
    // Start testing by adding new logo
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var mockGitHubFile = new GitHubFile();
    mockGitHubFile.setFileName(LOGO_FILE);
    mockGitHubFile.setType(FileType.LOGO);
    mockGitHubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(imageRepo.findByImageUrlEndsWithIgnoreCase(anyString())).thenReturn(List.of(getMockImage()));
    // Executes
    result = productService.syncLatestDataFromMarketRepo(false);

    verify(productRepo).deleteById(anyString());
    verify(imageRepo).deleteAllByProductId(anyString());
    verify(imageRepo).findByImageUrlEndsWithIgnoreCase(anyString());
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertFalse(result.isEmpty(), "Latest data from Market repo should not be empty");
  }

  // Using a random UUID in test; no dedicated constant/ID needed
  @SuppressWarnings("java:S5977")
  @Test
  void testUpdateNewLogoFromGitHubModifyLogo() throws IOException {
    // Start testing by adding new logo
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var mockGitHubFile = new GitHubFile();
    mockGitHubFile.setFileName("meta.json");
    mockGitHubFile.setType(FileType.META);
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(productRepo.findByMarketDirectory(anyString())).thenReturn(getMockProducts());

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);

    assertNotNull(result, "Latest data from Market repo should not be null");
    assertFalse(result.isEmpty(), "Latest data from Market repo should not be empty");
    verify(productRepo).deleteById(anyString());
    verify(imageRepo).deleteAllByProductId(anyString());
  }

  @Test
  void testSyncOneProduct() throws IOException {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_PATH);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(mockProduct));
    var mockContents = mockMetaJsonAndLogoList();
    when(marketRepoService.getMarketItemByPath(anyString())).thenReturn(mockContents);
    when(productRepo.save(any(Product.class))).thenReturn(mockProduct);
    assertTrue(productService.syncOneProduct(SAMPLE_PRODUCT_ID, SAMPLE_PRODUCT_PATH, false),
        "Sync one product should be successful when not overriding Market item path");
    assertTrue(productService.syncOneProduct(SAMPLE_PRODUCT_ID, SAMPLE_PRODUCT_PATH, true),
        "Sync one product should be successful when overriding Market item path");
  }

  private List<GHContent> mockMetaJsonAndLogoList() throws IOException {
    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);

    var mockContentLogo = mockGHContentAsLogo();
    return new ArrayList<>(List.of(mockContent, mockContentLogo));
  }

  @Test
  void testSyncOneProductFailed() {
    when(marketRepoService.getMarketItemByPath(anyString())).thenThrow(new MockitoException("Sync a product failed!"));
    assertFalse(productService.syncOneProduct(StringUtils.EMPTY, StringUtils.EMPTY, true),
        "Sync one product should be failed");
  }

  // Using a random UUID in test; no dedicated constant/ID needed
  @SuppressWarnings("java:S5977")
  @Test
  void testSyncProductsAsUpdateMetaJSONFromGitHubAddVendorLogo() throws IOException {
    // Start testing by adding new meta
    mockMarketRepoMetaStatus();
    var mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);

    var mockGithubFile = new GitHubFile();
    mockGithubFile.setFileName(META_FILE);
    mockGithubFile.setType(FileType.META);
    mockGithubFile.setStatus(FileStatus.MODIFIED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGithubFile));
    var mockGHContent = mockGHContentAsMetaJSON();
    when(gitHubService.getGHContent(any(), anyString(), any())).thenReturn(mockGHContent);
    when(mockGHContent.read()).thenReturn(this.getClass().getResourceAsStream(META_JSON_FILE_WITH_VENDOR_INFORMATION));
    when(productRepo.save(any(Product.class))).thenReturn(new Product());

    // Executes
    var result = productService.syncLatestDataFromMarketRepo(false);
    assertNotNull(result, "Latest data from Market repo should not be null");
    assertTrue(result.isEmpty(), "Latest data from Market repo should be empty");
  }

  @Test
  void testSyncFirstPublishedDateOfAllProducts() throws IOException {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_PATH);
    mockProduct.setRepositoryName(SAMPLE_PRODUCT_REPOSITORY_NAME);
    List<Product> products = List.of(mockProduct);
    when(productRepo.findAll()).thenReturn(products);
    when(productRepo.save(any(Product.class))).thenReturn(mockProduct);
    GHTag ghTagVersionOne = new GHTag();
    GHTag ghTagVersionTwo = new GHTag();
    List<GHTag> tags = Arrays.asList(ghTagVersionOne, ghTagVersionTwo);
    when(gitHubService.getRepositoryTags(SAMPLE_PRODUCT_REPOSITORY_NAME)).thenReturn(tags);
    assertTrue(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be successful");
  }

  @Test
  void testNoSyncFirstPublishedDateForSyncedProducts() {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setRepositoryName(SAMPLE_PRODUCT_REPOSITORY_NAME);
    mockProduct.setFirstPublishedDate(new Date());
    when(productRepo.findAll()).thenReturn(List.of(mockProduct));
    assertTrue(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be successful");
  }

  @Test
  void testSyncFirstPublishedDateWithFindingAllProductsFailed() {
    when(productRepo.findAll()).thenThrow(new MockitoException("Sync FirstPublishedDate of all products failed!"));
    assertFalse(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be failed");
  }

  @Test
  void testSyncFirstPublishedDateForNoProduct() {
    when(productRepo.findAll()).thenReturn(new ArrayList<>());
    assertTrue(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be successful with no product synced");
  }

  @Test
  void testSyncFirstPublishedDateOfAllProductsFailed() throws IOException {
    Product mockProduct = new Product();
    mockProduct.setRepositoryName(SAMPLE_PRODUCT_REPOSITORY_NAME);
    List<Product> products = List.of(mockProduct);
    when(productRepo.findAll()).thenReturn(products);
    when(gitHubService.getRepositoryTags(SAMPLE_PRODUCT_REPOSITORY_NAME)).thenThrow(
        new IOException("Mocked IOException"));
    when(productRepo.save(mockProduct)).thenThrow(
        new MockitoException("Mocked IOException"));
    assertFalse(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be failed");
  }

  @Test
  void testSyncFirstPublishedDateOfAllProductsSuccess() throws IOException {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_PATH);
    mockProduct.setRepositoryName(SAMPLE_PRODUCT_REPOSITORY_NAME);
    List<Product> products = List.of(mockProduct);
    when(productRepo.findAll()).thenReturn(products);
    when(productRepo.save(any(Product.class))).thenReturn(mockProduct);
    GHTag ghTagVersionOne = mock(GHTag.class);
    GHCommit commitOfTagVersionOne = mock(GHCommit.class);
    GHTag ghTagVersionTwo = mock(GHTag.class);
    GHCommit commitOfTagVersionTwo = mock(GHCommit.class);
    List<GHTag> tags = Arrays.asList(ghTagVersionOne, ghTagVersionTwo);
    when(ghTagVersionOne.getCommit()).thenReturn(commitOfTagVersionOne);
    when(commitOfTagVersionOne.getCommitDate()).thenReturn(new Date());
    when(ghTagVersionTwo.getCommit()).thenReturn(commitOfTagVersionTwo);
    when(commitOfTagVersionTwo.getCommitDate()).thenReturn(new Date());
    when(gitHubService.getRepositoryTags(SAMPLE_PRODUCT_REPOSITORY_NAME)).thenReturn(tags);
    assertTrue(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be successful");
  }

  @Test
  void testSyncFirstPublishedDateWithGettingTagCommitFailed() throws IOException {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_PATH);
    mockProduct.setRepositoryName(SAMPLE_PRODUCT_REPOSITORY_NAME);
    List<Product> products = List.of(mockProduct);
    when(productRepo.findAll()).thenReturn(products);
    GHTag ghTag = mock(GHTag.class);
    List<GHTag> tags = Collections.singletonList(ghTag);
    GHCommit ghCommit = mock(GHCommit.class);
    when(ghTag.getCommit()).thenReturn(ghCommit);
    when(ghCommit.getCommitDate()).thenThrow(
        new IOException("get commit date of tag commit failed!"));
    when(gitHubService.getRepositoryTags(SAMPLE_PRODUCT_REPOSITORY_NAME)).thenReturn(tags);
    assertTrue(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be successful with getting commit date of tag commit failed");

    GHTag ghTag2 = mock(GHTag.class);
    List<GHTag> secondTags = Arrays.asList(ghTag, ghTag2);
    GHCommit ghCommit2 = mock(GHCommit.class);
    when(ghTag2.getCommit()).thenReturn(ghCommit2);
    when(ghCommit2.getCommitDate()).thenReturn(new Date());
    when(gitHubService.getRepositoryTags(SAMPLE_PRODUCT_REPOSITORY_NAME)).thenReturn(secondTags);
    assertTrue(productService.syncFirstPublishedDateOfAllProducts(),
        "Sync first published date of all products should be successful with getting commit date of tag commit");
  }

  @Test
  void testGetGitHubReleaseModelByProductIdAndReleaseId() throws IOException {
    String mockProductId = "testProductId";
    String mockRepositoryName = "axonivy-market/portal";
    Long mockReleaseId = 1L;
    Product mockProduct = new Product();
    mockProduct.setId(mockProductId);
    mockProduct.setRepositoryName(mockRepositoryName);
    when(productRepo.findProductByIdAndRelatedData(mockProductId)).thenReturn(mockProduct);
    when(gitHubService.getGitHubReleaseModelByProductIdAndReleaseId(mockProduct, mockReleaseId))
        .thenReturn(new GitHubReleaseModel());

    GitHubReleaseModel result = productService.getGitHubReleaseModelByProductIdAndReleaseId(mockProductId,
        mockReleaseId);

    assertNotNull(result, "Github release model should not be null");
    verify(gitHubService).getGitHubReleaseModelByProductIdAndReleaseId(any(Product.class), anyLong());
  }

  @Test
  void testGetGitHubReleaseModels() throws IOException {
    String mockProductId = "testProductId";
    String mockRepositoryName = "axonivy-market/portal";
    String mockProductSourceUrl = "axonivy-market/portal";
    Pageable mockPageable = mock(Pageable.class);
    Product mockProduct = new Product();
    mockProduct.setId(mockProductId);
    mockProduct.setRepositoryName(mockRepositoryName);
    mockProduct.setSourceUrl(mockProductSourceUrl);

    when(productRepo.findProductByIdAndRelatedData(mockProductId)).thenReturn(mockProduct);
    when(gitHubService.getGitHubReleaseModels(anyList(), any(Pageable.class), anyString(), anyString(),
        anyString())).thenReturn(Page.empty());

    Page<GitHubReleaseModel> result = productService.getGitHubReleaseModels(mockProductId, mockPageable);

    assertNotNull(result, "Github release model should not be null");
    verify(gitHubService).getGitHubReleaseModels(anyList(), any(Pageable.class), anyString(), anyString(), anyString());
  }

  @Test
  void testGetGitHubReleaseModelsOfProductWithBlankSourceUrl() throws IOException {
    String mockProductId = "testProductId";
    Pageable mockPageable = PageRequest.of(0, 10);
    Product mockProduct = new Product();
    mockProduct.setId(mockProductId);
    mockProduct.setRepositoryName("axonivy-market/portal");
    mockProduct.setSourceUrl("");
    when(productRepo.findProductByIdAndRelatedData(mockProductId)).thenReturn(mockProduct);

    Page<GitHubReleaseModel> result = productService.getGitHubReleaseModels(mockProductId, mockPageable);

    assertNotNull(result, "Github release model list should not be null");
    assertEquals(0, result.getTotalElements(), "Github release model list should be empty when source url is blank");
    verify(productRepo).findProductByIdAndRelatedData(mockProductId);
    verifyNoInteractions(gitHubService);
  }

  @Test
  void testGetGitHubReleaseModelsOfProductWithBlankRepository() throws IOException {
    String mockProductId = "testProductId";
    Pageable mockPageable = PageRequest.of(0, 10);
    Product mockProduct = new Product();
    mockProduct.setId(mockProductId);
    mockProduct.setRepositoryName("");
    mockProduct.setSourceUrl("https://github.com/axonivy-market/portal");
    when(productRepo.findProductByIdAndRelatedData(mockProductId)).thenReturn(mockProduct);

    Page<GitHubReleaseModel> result = productService.getGitHubReleaseModels(mockProductId, mockPageable);

    assertNotNull(result, "Github release model list should not be null");
    assertEquals(0, result.getTotalElements(),
        "Github release model list should be empty when repository is blank");
    verify(productRepo).findProductByIdAndRelatedData(mockProductId);
    verifyNoInteractions(gitHubService);
  }

  @Test
  void testGetProductIdList() {
    List<Product> products = List.of(new Product(), new Product());
    when(productRepo.findAll()).thenReturn(products);

    List<String> result = productService.getProductIdList();
    assertEquals(products.size(), result.size(),
        "Github release model list size should match total products size");
  }

  @Test
  void testSyncGitHubReleaseModels() throws IOException {
    Product mockProduct = new Product();
    mockProduct.setId("portal");
    mockProduct.setRepositoryName(SAMPLE_PRODUCT_REPOSITORY_NAME);
    mockProduct.setSourceUrl("https://github.com/axonivy-market/portal");
    when(productRepo.findProductByIdAndRelatedData(anyString())).thenReturn(mockProduct);
    productService.syncGitHubReleaseModels(SAMPLE_PRODUCT_ID, PAGEABLE);

    verify(productService, times(1)).getGitHubReleaseModels(SAMPLE_PRODUCT_ID, PAGEABLE);
  }

  @Test
  void testGetBestMatchVersionSuccess() {
    String productId = "123";
    String inputVersion = "1";
    List<String> repoVersions = List.of("1.0.0", "1.0.1", "1.0.2", "1.0.2-SNAPSHOT");
    String bestMatchVersion = "1.0.2";

    when(productRepo.getReleasedVersionsById(productId)).thenReturn(repoVersions);
    String result = productService.getBestMatchVersion(productId, inputVersion, false);
    assertEquals(bestMatchVersion, result, "Should return correct version");
    verify(productRepo).getReleasedVersionsById(productId);
  }
}
