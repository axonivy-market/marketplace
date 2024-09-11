package com.axonivy.market.service.impl;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static com.axonivy.market.enums.DocumentField.SHORT_DESCRIPTIONS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.axonivy.market.criteria.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GitHubRepoMeta;
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
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseSetup {

  private static final long LAST_CHANGE_TIME = 1718096290000L;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  public static final String RELEASE_TAG = "v10.0.2";
  private static final String INSTALLATION_FILE_PATH = "src/test/resources/installationCount.json";
  private static final String EMPTY_SOURCE_URL_META_JSON_FILE = "/emptySourceUrlMeta.json";

  private String keyword;
  private String language;
  private Page<Product> mockResultReturn;

  @Mock
  private MongoTemplate mongoTemplate;

  @Mock
  private GHRepository ghRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductModuleContentRepository productModuleContentRepository;

  @Mock
  private GHAxonIvyMarketRepoService marketRepoService;

  @Mock
  private GitHubRepoMetaRepository repoMetaRepository;

  @Mock
  private GitHubService gitHubService;

  @Mock
  private ProductCustomSortRepository productCustomSortRepository;

  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);

  @Captor
  ArgumentCaptor<ArrayList<ProductModuleContent>> argumentCaptorProductModuleContents;

  @Captor
  ArgumentCaptor<ProductModuleContent> argumentCaptorProductModuleContent;

  @Mock
  private GHAxonIvyProductRepoService ghAxonIvyProductRepoService;

  @Captor
  ArgumentCaptor<ArrayList<Product>> productListArgumentCaptor;

  @Captor
  ArgumentCaptor<ProductSearchCriteria> productSearchCriteriaArgumentCaptor;

  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  public void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testUpdateInstallationCountForProduct() {
    String designerVersion = "10.0.20";
    int result = productService.updateInstallationCountForProduct(null, designerVersion);
    assertEquals(0, result);

    Product product = mockProduct();
    when(productRepository.getProductById(product.getId())).thenReturn(product);
    when(productRepository.increaseInstallationCount(product.getId())).thenReturn(31);
    result = productService.updateInstallationCountForProduct(product.getId(), designerVersion);
    assertEquals(31, result);

    result = productService.updateInstallationCountForProduct(product.getId(), "");
    assertEquals(31, result);
  }

  @Test
  void testSyncInstallationCountWithNewProduct() {
    Product product = new Product();
    product.setSynchronizedInstallationCount(null);
    product.setId("portal");
    ReflectionTestUtils.setField(productService, "installationCountPath", INSTALLATION_FILE_PATH);

    productService.syncInstallationCountWithProduct(product);

    assertTrue(product.getInstallationCount() >= 20 && product.getInstallationCount() <= 50);
    assertTrue(product.getSynchronizedInstallationCount());
  }

  @Test
  void testSyncInstallationCountWithProduct() {
    ReflectionTestUtils.setField(productService, "installationCountPath", INSTALLATION_FILE_PATH);
    Product product = mockProduct();
    product.setSynchronizedInstallationCount(false);

    productService.syncInstallationCountWithProduct(product);

    assertEquals(40, product.getInstallationCount());
    assertEquals(true, product.getSynchronizedInstallationCount());
    assertTrue(product.getSynchronizedInstallationCount());
  }

  private Product mockProduct() {
    return Product.builder().id("google-maps-connector").language("English").synchronizedInstallationCount(true)
        .build();
  }

  @Test
  void testFindProducts() {
    language = "en";
    // Start testing by All
    when(productRepository.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
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
    verify(productRepository).searchByCriteria(productSearchCriteriaArgumentCaptor.capture(), any(Pageable.class));
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

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);

    // Start testing by deleting new meta
    mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    mockGithubFile.setStatus(FileStatus.REMOVED);
    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);
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
    var mockGHContent = mockGHContentAsMetaJSON();
    when(gitHubService.getGHContent(any(), anyString(), any())).thenReturn(mockGHContent);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(gitHubService.getGHContent(any(), anyString(), any())).thenReturn(mockGHContent);
    when(productRepository.findByLogoUrl(any())).thenReturn(new Product());

    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);
  }

  @Test
  void testFindAllProductsWithKeyword() {
    language = "en";
    when(productRepository.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result);
    verify(productRepository).searchByCriteria(any(), any(Pageable.class));

    // Test has keyword
    when(productRepository.searchByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME))
            .toList()));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()));

    // Test has keyword and type is connector
    when(productRepository.searchByCriteria(any(), any(Pageable.class)))
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
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);
    when(ghAxonIvyProductRepoService.getReadmeAndProductContentsFromTag(any(), any(), anyString())).thenReturn(
        mockReadmeProductContent());
    when(gitHubService.getRepository(any())).thenReturn(ghRepository);

    GHTag mockTag = mock(GHTag.class);
    GHCommit mockGHCommit = mock(GHCommit.class);

    when(mockTag.getName()).thenReturn(RELEASE_TAG);
    when(mockTag.getCommit()).thenReturn(mockGHCommit);
    when(mockGHCommit.getCommitDate()).thenReturn(new Date());

    when(gitHubService.getRepositoryTags(anyString())).thenReturn(List.of(mockTag));
    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);
    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, List.of(mockContent));
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);
    when(productModuleContentRepository.saveAll(anyList())).thenReturn(List.of(mockReadmeProductContent()));

    // Executes
    productService.syncLatestDataFromMarketRepo();
    verify(productModuleContentRepository).saveAll(argumentCaptorProductModuleContents.capture());
    verify(productRepository).save(argumentCaptor.capture());

    assertThat(argumentCaptorProductModuleContents.getValue()).usingRecursiveComparison()
        .isEqualTo(List.of(mockReadmeProductContent()));
  }

  @Test
  void testSyncProductsFirstTimeWithOutSourceUrl() throws IOException {
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(EMPTY_SOURCE_URL_META_JSON_FILE);
    when(mockContent.read()).thenReturn(inputStream);

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, List.of(mockContent));
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

    // Executes
    productService.syncLatestDataFromMarketRepo();
    verify(productModuleContentRepository).save(argumentCaptorProductModuleContent.capture());
    assertEquals("1.0", argumentCaptorProductModuleContent.getValue().getTag());
  }

  @Test
  void testSyncProductsSecondTime() throws IOException {
    var gitHubRepoMeta = mock(GitHubRepoMeta.class);
    when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

    when(productRepository.findAll()).thenReturn(mockProducts());

    GHCommit mockGHCommit = mock(GHCommit.class);

    GHTag mockTag = mock(GHTag.class);
    when(mockTag.getName()).thenReturn("v10.0.2");
    when(mockTag.getCommit()).thenReturn(mockGHCommit);

    GHTag mockTag2 = mock(GHTag.class);
    when(mockTag2.getName()).thenReturn("v10.0.3");

    when(mockGHCommit.getCommitDate()).thenReturn(new Date());
    when(gitHubService.getRepositoryTags(anyString())).thenReturn(Arrays.asList(mockTag, mockTag2));

    ProductModuleContent mockReturnProductContent = mockReadmeProductContent();
    mockReturnProductContent.setTag("v10.0.3");

    when(ghAxonIvyProductRepoService.getReadmeAndProductContentsFromTag(any(), any(), anyString()))
        .thenReturn(mockReturnProductContent);
    when(productModuleContentRepository.saveAll(anyList()))
        .thenReturn(List.of(mockReadmeProductContent(), mockReturnProductContent));

    // Executes
    productService.syncLatestDataFromMarketRepo();

    verify(productModuleContentRepository).saveAll(argumentCaptorProductModuleContents.capture());
    verify(productRepository).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getProductModuleContent()).usingRecursiveComparison()
        .isEqualTo(mockReadmeProductContent());
  }

  @Test
  void testNothingToSync() {
    var gitHubRepoMeta = mock(GitHubRepoMeta.class);
    when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertTrue(result);
  }

  @Test
  void testSyncNullProductModuleContent() {
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);
    
    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, new ArrayList<>());
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

    // Executes
    productService.syncLatestDataFromMarketRepo();
    verify(productRepository).save(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getProductModuleContent()).isNull();
  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = TypeOption.ALL.getOption();
    keyword = "on";
    language = "en";
    when(productRepository.searchByCriteria(any(), any(Pageable.class))).thenReturn(
        mockResultReturn);

    var result = productService.findProducts(type, keyword, language, false, simplePageable);
    assertEquals(result, mockResultReturn);
    verify(productRepository).searchByCriteria(any(), any(Pageable.class));
  }

  @Test
  void testFetchProductDetail() {
    String id = "amazon-comprehend";
    Product mockProduct = mockResultReturn.getContent().get(0);
    mockProduct.setSynchronizedInstallationCount(true);
    when(productRepository.getProductById(id)).thenReturn(mockProduct);
    Product result = productService.fetchProductDetail(id);
    assertEquals(mockProduct, result);
    verify(productRepository, times(1)).getProductById(id);
  }

  @Test
  void testFetchProductDetailByIdAndVersion() {
    String id = "amazon-comprehend";
    Product mockProduct = mockResultReturn.getContent().get(0);
    when(productRepository.getProductByIdAndTag(id, RELEASE_TAG)).thenReturn(mockProduct);
    Product result = productService.fetchProductDetailByIdAndVersion(id, "10.0.2");
    assertEquals(mockProduct, result);
    verify(productRepository, times(1)).getProductByIdAndTag(id, RELEASE_TAG);
  }

  @Test
  void testFetchBestMatchProductDetailByIdAndVersion() {
    String id = "amazon-comprehend";
    Product mockProduct = mockResultReturn.getContent().get(0);
    mockProduct.setSynchronizedInstallationCount(true);
    when(productRepository.getReleasedVersionsById(id)).thenReturn(List.of("10.0.2", "10.0.1"));
    when(productRepository.getProductByIdAndTag(id, RELEASE_TAG)).thenReturn(mockProduct);
    Product result = productService.fetchBestMatchProductDetail(id, "10.0.2");
    assertEquals(mockProduct, result);
    verify(productRepository, times(1)).getProductByIdAndTag(id, RELEASE_TAG);
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

    verify(mongoTemplate, times(1)).updateMulti(any(Query.class), any(Update.class), eq(Product.class));
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort() throws InvalidParamException {
    // prepare
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    when(productRepository.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

    List<Product> refinedProducts = productService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts);

    assertEquals(1, refinedProducts.size());
    assertEquals(1, refinedProducts.get(0).getCustomOrder());
    verify(productRepository, times(1)).findById(SAMPLE_PRODUCT_ID);
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort_ProductNotFound() {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepository.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.empty());

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
    when(productRepository.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

    productService.addCustomSortProduct(customSortRequest);

    verify(productCustomSortRepository, times(1)).deleteAll();
    verify(mongoTemplate, times(1)).updateMulti(any(Query.class), any(Update.class), eq(Product.class));
    verify(productCustomSortRepository, times(1)).save(any(ProductCustomSort.class));
    verify(productRepository, times(1)).saveAll(productListArgumentCaptor.capture());

    List<Product> capturedProducts = productListArgumentCaptor.getValue();
    assertEquals(1, capturedProducts.size());
    assertEquals(1, capturedProducts.get(0).getCustomOrder());
  }

  @Test
  void testUpdateProductInstallationCountWhenNotSynchronized() {
    Product product = mockProduct();
    product.setSynchronizedInstallationCount(false);
    String id = product.getId();
    ReflectionTestUtils.setField(productService, "installationCountPath", INSTALLATION_FILE_PATH);

    when(productRepository.updateInitialCount(eq(id), anyInt())).thenReturn(10);

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

    verify(repoMetaRepository).deleteAll();
    verify(productRepository).deleteAll();
  }

  private void mockMarketRepoMetaStatus() {
    var mockMarketRepoMeta = new GitHubRepoMeta();
    mockMarketRepoMeta.setRepoURL(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMarketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMarketRepoMeta.setLastChange(LAST_CHANGE_TIME);
    mockMarketRepoMeta.setLastSHA1(SHA1_SAMPLE);
    when(repoMetaRepository.findByRepoName(any())).thenReturn(mockMarketRepoMeta);
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

  private ProductModuleContent mockReadmeProductContent() {
    ProductModuleContent productModuleContent = new ProductModuleContent();
    productModuleContent.setId("123");
    productModuleContent.setTag("v10.0.2");
    productModuleContent.setName("Amazon Comprehend");
    Map<String, String> description = new HashMap<>();
    description.put(Language.EN.getValue(), "testDescription");
    productModuleContent.setDescription(description);
    return productModuleContent;
  }

  private List<Product> mockProducts() {
    Product product1 = Product.builder().repositoryName("axonivy-market/amazon-comprehend-connector")
        .productModuleContent(mockReadmeProductContent()).build();
    return List.of(product1);
  }
}
