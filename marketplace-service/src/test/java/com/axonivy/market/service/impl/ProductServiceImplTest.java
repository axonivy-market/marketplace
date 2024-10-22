package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.util.MavenUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseSetup {
  private static final long LAST_CHANGE_TIME = 1718096290000L;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  private static final String INSTALLATION_FILE_PATH = "src/test/resources/installationCount.json";
  private static final String EMPTY_SOURCE_URL_META_JSON_FILE = "/emptySourceUrlMeta.json";
  private static final String META_JSON_FILE_WITH_VENDOR_INFORMATION = "/meta-with-vendor-information.json";
  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
  @Captor
  ArgumentCaptor<ArrayList<ProductModuleContent>> argumentCaptorProductModuleContents;
  @Captor
  ArgumentCaptor<ProductModuleContent> argumentCaptorProductModuleContent;
  @Captor
  ArgumentCaptor<ArrayList<Product>> productListArgumentCaptor;
  @Captor
  ArgumentCaptor<ProductSearchCriteria> productSearchCriteriaArgumentCaptor;
  private String keyword;
  private String language;
  private Page<Product> mockResultReturn;
  @Mock
  private MongoTemplate mongoTemplate;
  @Mock
  private GHRepository ghRepository;
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
  private ProductCustomSortRepository productCustomSortRepo;
  @Mock
  private ImageService imageService;
  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepo;
  @Mock
  private ProductContentService productContentService;
  @Mock
  private GHAxonIvyProductRepoService axonIvyProductRepoService;
  @Mock
  private MetadataSyncRepository metadataSyncRepo;
  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  public void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testUpdateInstallationCountForProduct() {
    int result = productService.updateInstallationCountForProduct(null, MOCK_RELEASED_VERSION);
    assertEquals(0, result);

    Product product = getMockProduct();
    product.setSynchronizedInstallationCount(true);
    when(productRepo.getProductById(MOCK_PRODUCT_ID)).thenReturn(product);
    when(productRepo.increaseInstallationCount(MOCK_PRODUCT_ID)).thenReturn(31);

    result = productService.updateInstallationCountForProduct(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(31, result);

    result = productService.updateInstallationCountForProduct(MOCK_PRODUCT_ID, StringUtils.EMPTY);
    assertEquals(31, result);
  }

  @Test
  void testSyncInstallationCountWithNewProduct() {
    Product product = new Product();
    product.setSynchronizedInstallationCount(null);
    product.setId(MOCK_PRODUCT_ID);
    ReflectionTestUtils.setField(productService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME, INSTALLATION_FILE_PATH);

    productService.syncInstallationCountWithProduct(product);

    assertTrue(product.getInstallationCount() >= 20 && product.getInstallationCount() <= 50);
    assertTrue(product.getSynchronizedInstallationCount());
  }

  @Test
  void testSyncInstallationCountWithProduct() {
    ReflectionTestUtils.setField(productService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME, INSTALLATION_FILE_PATH);
    Product product = getMockProduct();
    product.setSynchronizedInstallationCount(false);

    productService.syncInstallationCountWithProduct(product);

    assertEquals(40, product.getInstallationCount());
    assertTrue(product.getSynchronizedInstallationCount());
  }

  @Test
  void testFindProducts() {
    language = "en";
    // Start testing by All
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Connector
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Other
    // Executes
    result = productService.findProducts(TypeOption.DEMOS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(2, result.getSize());
  }

  @Test
  void testFindProductsInRESTClientOfDesigner() {
    productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, Language.EN.getValue(), true, PAGEABLE);
    verify(productRepo).searchByCriteria(productSearchCriteriaArgumentCaptor.capture(), any(Pageable.class));
    assertEquals(List.of(SHORT_DESCRIPTIONS), productSearchCriteriaArgumentCaptor.getValue().getExcludeFields());
  }

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
    var result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());

    // Start testing by deleting new meta
    mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    mockGithubFile.setStatus(FileStatus.REMOVED);
    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

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
    var result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));

    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testFindAllProductsWithKeyword() {
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result);
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));

    // Test has keyword
    when(productRepo.searchByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME))
            .toList()));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()));

    // Test has keyword and type is connector
    when(productRepo.searchByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME)
                && product.getType().equals(TypeOption.CONNECTORS.getCode()))
            .toList()));
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, language, false,
        PAGEABLE);
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()));
  }

  @Test
  void testSyncProductsFirstTime() throws IOException {
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(null);
    when(productContentService.getReadmeAndProductContentsFromTag(any(), anyString(), anyString(), any())).thenReturn(
        mockReadmeProductContent());

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, mockMetaJsonAndLogoList());
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
    when(productModuleContentRepo.saveAll(anyList())).thenReturn(List.of(mockReadmeProductContent()));

    when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean()))
        .thenReturn(GHAxonIvyProductRepoServiceImplTest.mockImage());
    when(productRepo.save(any(Product.class))).thenReturn(new Product());
    // Executes
    productService.syncLatestDataFromMarketRepo();
    verify(productModuleContentRepo).saveAll(argumentCaptorProductModuleContents.capture());
    verify(productRepo).save(argumentCaptor.capture());

    assertEquals(7, argumentCaptorProductModuleContents.getValue().size());
    assertThat(argumentCaptorProductModuleContents.getValue().get(0).getId())
        .isEqualTo(mockReadmeProductContent().getId());
  }

  @Test
  void testSyncProductsFirstTimeWithOutSourceUrl() throws IOException {
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
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
    when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(
        GHAxonIvyProductRepoServiceImplTest.mockImage());
    when(productRepo.save(any(Product.class))).thenReturn(new Product());
    // Executes
    productService.syncLatestDataFromMarketRepo();
    verify(productModuleContentRepo).save(argumentCaptorProductModuleContent.capture());
    assertEquals("1.0", argumentCaptorProductModuleContent.getValue().getTag());
  }

  @Test
  void testSyncProductsSecondTime() {
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      Product mockProduct = getMockProduct();
      mockProduct.setProductModuleContent(mockReadmeProductContent());
      mockProduct.setRepositoryName("axonivy-market/bpmn-statistic");
      var gitHubRepoMeta = mock(GitHubRepoMeta.class);
      when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
      var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
      when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
      when(repoMetaRepo.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

      when(productRepo.findAll()).thenReturn(List.of(mockProduct));

      ProductModuleContent mockReturnProductContent = mockReadmeProductContent();
      mockReturnProductContent.setTag("10.0.3");

      when(productContentService.getReadmeAndProductContentsFromTag(any(), anyString(), anyString(), any()))
          .thenReturn(mockReturnProductContent);
      when(productModuleContentRepo.saveAll(anyList()))
          .thenReturn(List.of(mockReadmeProductContent(), mockReturnProductContent));
      when(MavenUtils.getMetadataContentFromUrl(any())).thenReturn(getMockMetadataContent());
      when(MavenUtils.buildDownloadUrl(any(), any(), any(), any(), any(), any())).thenReturn(
          "https://maven.axonivy.com/com/axonivy/demo/connectivity-demos-product/12.0" +
              ".0-SNAPSHOT/connectivity-demos-product-12.0.0-20241017.220639-51.zip");
      // Executes
      productService.syncLatestDataFromMarketRepo();

      verify(productModuleContentRepo).saveAll(argumentCaptorProductModuleContents.capture());
      verify(productRepo).save(argumentCaptor.capture());
      assertThat(argumentCaptor.getValue().getProductModuleContent().getId())
          .isEqualTo(mockReadmeProductContent().getId());
    }
  }

  @Test
  void testNothingToSync() {
    var gitHubRepoMeta = mock(GitHubRepoMeta.class);
    when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  //TODO
//  @Test
//  void testSyncNullProductModuleContent() {
//    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
//    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
//    when(repoMetaRepo.findByRepoName(anyString())).thenReturn(null);
//
//    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
//    mockGHContentMap.put(SAMPLE_PRODUCT_ID, new ArrayList<>());
//    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
//    when(productRepo.save(any(Product.class))).thenReturn(new Product());
//
//    // Executes
//    productService.syncLatestDataFromMarketRepo();
//    verify(productRepo).save(argumentCaptor.capture());
//
//    assertThat(argumentCaptor.getValue().getProductModuleContent()).isNull();
//  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = TypeOption.ALL.getOption();
    keyword = "on";
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(
        mockResultReturn);

    var result = productService.findProducts(type, keyword, language, false, simplePageable);
    assertEquals(result, mockResultReturn);
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));
  }

  @Test
  void testFetchProductDetail() {
    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersionWithData();
    Product mockProduct = getMockProduct();
    when(mavenArtifactVersionRepo.findById(MOCK_PRODUCT_ID)).thenReturn(
        Optional.ofNullable(mockMavenArtifactVersion));
    when(productRepo.getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(null);
    mockProduct.setSynchronizedInstallationCount(true);
    Product result = productService.fetchProductDetail(MOCK_PRODUCT_ID, true);
    assertNull(result);
  }

  @Test
  void testGetProductByIdWithNewestReleaseVersion() {
    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersionWithData();
    Product mockProduct = getMockProduct();

    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> mavenArtifactVersionRepo.findById(MOCK_PRODUCT_ID)).thenReturn(
          Optional.of(mockMavenArtifactVersion));
      when(MavenUtils.getAllExistingVersions(mockMavenArtifactVersion, true, StringUtils.EMPTY))
          .thenReturn(List.of(MOCK_SNAPSHOT_VERSION));

      when(productRepo.getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);
      when(productJsonContentRepo.findByProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION))
          .thenReturn(List.of(getMockProductJsonContentContainMavenDropins()));

      Product result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
      assertEquals(mockProduct, result);

      when(mavenArtifactVersionRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.empty());
      when(productRepo.getReleasedVersionsById(MOCK_PRODUCT_ID)).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
      when(productRepo.getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);
      result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
      assertEquals(mockProduct, result);
    }
  }

  @Test
  void testFetchProductDetailByIdAndVersion() {

    Product mockProduct = mockResultReturn.getContent().get(0);
    when(productRepo.getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(mockProduct);

    Product result = productService.fetchProductDetailByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);

    assertEquals(mockProduct, result);
    verify(productRepo).getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
  }

  @Test
  void testFetchBestMatchProductDetailByIdAndVersion() {
    ReflectionTestUtils.setField(productService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME, INSTALLATION_FILE_PATH);
    Product mockProduct = getMockProduct();
    Metadata mockMetadata = getMockMetadataWithVersions();
    mockMetadata.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    when(metadataRepo.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockMetadata));
    when(productRepo.getProductByIdAndTag(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(mockProduct);
    Product result = productService.fetchBestMatchProductDetail(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, result);
  }

  @Test
  void testGetCompatibilityFromNumericTag() {

    String result = productService.getCompatibilityFromOldestTag("1.0.0");
    assertEquals("1.0+", result);

    result = productService.getCompatibilityFromOldestTag("8");
    assertEquals("8.0+", result);

    result = productService.getCompatibilityFromOldestTag("11.2");
    assertEquals("11.2+", result);
  }

  @Test
  void testRemoveFieldFromAllProductDocuments() {
    productService.removeFieldFromAllProductDocuments("customOrder");

    verify(mongoTemplate).updateMulti(any(Query.class), any(Update.class), eq(Product.class));
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort() throws InvalidParamException {
    // prepare
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

    List<Product> refinedProducts = productService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts);

    assertEquals(1, refinedProducts.size());
    assertEquals(1, refinedProducts.get(0).getCustomOrder());
    verify(productRepo).findById(SAMPLE_PRODUCT_ID);
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort_ProductNotFound() {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.empty());

    InvalidParamException exception = assertThrows(InvalidParamException.class,
        () -> productService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts));
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
  }

  @Test
  void testAddCustomSortProduct() throws InvalidParamException {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    ProductCustomSortRequest customSortRequest = new ProductCustomSortRequest();
    customSortRequest.setOrderedListOfProducts(orderedListOfProducts);
    customSortRequest.setRuleForRemainder(SortOption.ALPHABETICALLY.getOption());

    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

    productService.addCustomSortProduct(customSortRequest);

    verify(productCustomSortRepo).deleteAll();
    verify(mongoTemplate).updateMulti(any(Query.class), any(Update.class), eq(Product.class));
    verify(productCustomSortRepo).save(any(ProductCustomSort.class));
    verify(productRepo).saveAll(productListArgumentCaptor.capture());

    List<Product> capturedProducts = productListArgumentCaptor.getValue();
    assertEquals(1, capturedProducts.size());
    assertEquals(1, capturedProducts.get(0).getCustomOrder());
  }

  @Test
  void testUpdateProductInstallationCountWhenNotSynchronized() {
    Product product = getMockProduct();
    product.setSynchronizedInstallationCount(false);
    String id = product.getId();
    ReflectionTestUtils.setField(productService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME, INSTALLATION_FILE_PATH);

    when(productRepo.updateInitialCount(eq(id), anyInt())).thenReturn(10);

    productService.updateProductInstallationCount(id, product);

    assertEquals(10, product.getInstallationCount());
  }

  @Test
  void testCreateOrder() {
    Sort.Order order = productService.createOrder(SortOption.ALPHABETICALLY, "en");

    assertEquals(Sort.Direction.ASC, order.getDirection());
    assertEquals(SortOption.ALPHABETICALLY.getCode("en"), order.getProperty());
  }

  @Test
  void testClearAllProducts() {
    productService.clearAllProducts();

    verify(repoMetaRepo).deleteAll();
    verify(productRepo).deleteAll();
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
    productModuleContent.setId(MOCK_PRODUCT_ID_WITH_TAG);
    productModuleContent.setTag(MOCK_RELEASED_VERSION);
    productModuleContent.setName(MOCK_PRODUCT_NAME);
    Map<String, String> description = new HashMap<>();
    description.put(Language.EN.getValue(), "testDescription");
    productModuleContent.setDescription(description);
    return productModuleContent;
  }

  @Test
  void testUpdateNewLogoFromGitHub_removeOldLogo() throws IOException {
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
    var result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(imageRepo.findByImageUrlEndsWithIgnoreCase(anyString()))
        .thenReturn(List.of(GHAxonIvyProductRepoServiceImplTest.mockImage()));
    // Executes
    result = productService.syncLatestDataFromMarketRepo();

    verify(productRepo).deleteById(anyString());
    verify(imageRepo).deleteAllByProductId(anyString());
    verify(imageRepo).findByImageUrlEndsWithIgnoreCase(anyString());
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void testUpdateNewLogoFromGitHub_ModifyLogo() throws IOException {
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
    var result = productService.syncLatestDataFromMarketRepo();

    assertNotNull(result);
    assertFalse(result.isEmpty());
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
    when(metadataService.syncProductMetadata(any(Product.class))).thenReturn(true);
    when(productRepo.save(any(Product.class))).thenReturn(mockProduct);
    // Executes
    var result = productService.syncOneProduct(SAMPLE_PRODUCT_PATH, SAMPLE_PRODUCT_ID, false);
    assertTrue(result);
  }

  private List<GHContent> mockMetaJsonAndLogoList() throws IOException {
    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);

    var mockContentLogo = mockGHContentAsLogo();
    return new ArrayList<>(List.of(mockContent, mockContentLogo));
  }

  @Test
  void testSyncProductsAsUpdateMetaJSONFromGitHub_AddVendorLogo() throws IOException {
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
    var result = productService.syncLatestDataFromMarketRepo();
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
