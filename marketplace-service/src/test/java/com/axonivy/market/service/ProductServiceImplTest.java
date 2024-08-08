package com.axonivy.market.service;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.axonivy.market.criteria.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.PagedIterable;
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
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseSetup {

  private static final long LAST_CHANGE_TIME = 1718096290000L;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  public static final String RELEASE_TAG = "v10.0.2";
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
  private GHAxonIvyMarketRepoService marketRepoService;

  @Mock
  private GitHubRepoMetaRepository repoMetaRepository;

  @Mock
  private GitHubService gitHubService;

  @Mock
  private ProductCustomSortRepository productCustomSortRepository;

  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
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
  void testUpdateInstallationCount() {
    // prepare
    Mockito.when(productRepository.findById("google-maps-connector")).thenReturn(Optional.of(mockProduct()));

    // exercise
    productService.updateInstallationCountForProduct("google-maps-connector");

    // Verify
    verify(productRepository).save(argumentCaptor.capture());
    int updatedInstallationCount = argumentCaptor.getValue().getInstallationCount();

    assertEquals(1, updatedInstallationCount);
    verify(productRepository, times(1)).findById(Mockito.anyString());
    verify(productRepository, times(1)).save(Mockito.any());
  }

  @Test
  void testSyncInstallationCountWithProduct() throws Exception {
    // Mock data
    ReflectionTestUtils.setField(productService, "installationCountPath", "path/to/installationCount.json");
    Product product = mockProduct();
    product.setSynchronizedInstallationCount(false);
    Mockito.when(productRepository.findById("google-maps-connector")).thenReturn(Optional.of(product));
    Mockito.when(productRepository.save(any())).thenReturn(product);
    // Mock the behavior of Files.readString and ObjectMapper.readValue
    String installationCounts = "{\"google-maps-connector\": 10}";
    try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
      when(Files.readString(Paths.get("path/to/installationCount.json"))).thenReturn(installationCounts);
      // Call the method
      int result = productService.updateInstallationCountForProduct("google-maps-connector");

      // Verify the results
      assertEquals(11, result);
      assertEquals(true, product.getSynchronizedInstallationCount());
      assertTrue(product.getSynchronizedInstallationCount());
    }
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
    when(gitHubService.getGHContent(any(), anyString(), anyString())).thenReturn(mockGHContent);

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

    var mockGitHubFile = mock(GitHubFile.class);
    mockGitHubFile = new GitHubFile();
    mockGitHubFile.setFileName(LOGO_FILE);
    mockGitHubFile.setType(FileType.LOGO);
    mockGitHubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    var mockGHContent = mockGHContentAsMetaJSON();
    when(gitHubService.getGHContent(any(), anyString(), anyString())).thenReturn(mockGHContent);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(gitHubService.getGHContent(any(), anyString(), anyString())).thenReturn(mockGHContent);
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
            .collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()));

    // Test has keyword and type is connector
    when(productRepository.searchByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME)
                && product.getType().equals(TypeOption.CONNECTORS.getCode()))
            .collect(Collectors.toList())));
    // Executes
    result =
        productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
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
    PagedIterable<GHTag> pagedIterable = Mockito.mock(String.valueOf(GHTag.class));
    when(ghRepository.listTags()).thenReturn(pagedIterable);

    GHTag mockTag = mock(GHTag.class);
    GHCommit mockGHCommit = mock(GHCommit.class);

    when(mockTag.getName()).thenReturn(RELEASE_TAG);
    when(mockTag.getCommit()).thenReturn(mockGHCommit);
    when(mockGHCommit.getCommitDate()).thenReturn(new Date());

    when(pagedIterable.toList()).thenReturn(List.of(mockTag));

    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, List.of(mockContent));
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

    // Executes
    productService.syncLatestDataFromMarketRepo();

    verify(productRepository).saveAll(productListArgumentCaptor.capture());

    assertThat(productListArgumentCaptor.getValue().get(0).getProductModuleContents()).usingRecursiveComparison()
        .isEqualTo(List.of(mockReadmeProductContent()));
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
    when(productRepository.findById(id)).thenReturn(Optional.of(mockProduct));
    Product result = productService.fetchProductDetail(id);
    assertEquals(mockProduct, result);
    verify(productRepository, times(1)).findById(id);
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

    InvalidParamException exception = assertThrows(InvalidParamException.class, () ->
        productService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts));
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
    productModuleContent.setTag("v10.0.2");
    productModuleContent.setName("Amazon Comprehend");
    Map<String, String> description = new HashMap<>();
    description.put(Language.EN.getValue(), "testDescription");
    productModuleContent.setDescription(description);
    return productModuleContent;
  }
}
