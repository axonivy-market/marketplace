package com.axonivy.market.service;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Product;
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
import com.axonivy.market.repository.GitHubRepoMetaRepository;
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
  private String langague;
  private Page<Product> mockResultReturn;

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

  @Captor
  ArgumentCaptor<Product> argumentCaptor = ArgumentCaptor.forClass(Product.class);
  @Mock
  private GHAxonIvyProductRepoService ghAxonIvyProductRepoService;

  @Captor
  ArgumentCaptor<ArrayList<Product>> productListArgumentCaptor;

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
    langague = "en";
    // Start testing by All
    when(productRepository.findAllListed(any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, langague, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Connector
    when(productRepository.searchListedByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, langague, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Other
    // Executes
    result = productService.findProducts(TypeOption.DEMOS.getOption(), keyword, langague, PAGEABLE);
    assertEquals(0, result.getSize());
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
    assertEquals(false, result);

    // Start testing by deleting new meta
    mockCommit = mockGHCommitHasSHA1(UUID.randomUUID().toString());
    when(mockCommit.getCommitDate()).thenReturn(new Date());
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    mockGithubFile.setStatus(FileStatus.REMOVED);
    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertEquals(false, result);
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
    assertEquals(false, result);

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(gitHubService.getGHContent(any(), anyString(), anyString())).thenReturn(mockGHContent);
    when(productRepository.findByLogoUrl(any())).thenReturn(new Product());

    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertEquals(false, result);
  }

  @Test
  void testFindAllProductsWithKeyword() throws IOException {
    langague = "en";
    when(productRepository.findAllListed(any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, langague, PAGEABLE);
    assertEquals(mockResultReturn, result);
    verify(productRepository).findAllListed(any(Pageable.class));

    // Test has keyword
    when(productRepository.searchListedByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME))
            .collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, langague, PAGEABLE);
    verify(productRepository).findAllListed(any(Pageable.class));
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()));

    // Test has keyword and type is connector
    when(productRepository.searchListedByCriteria(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME)
                && product.getType().equals(TypeOption.CONNECTORS.getCode()))
            .collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, langague, PAGEABLE);
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
  void testNothingToSync() throws IOException {
    var gitHubRepoMeta = mock(GitHubRepoMeta.class);
    when(gitHubRepoMeta.getLastSHA1()).thenReturn(SHA1_SAMPLE);
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(gitHubRepoMeta);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertEquals(true, result);
  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = TypeOption.ALL.getOption();
    keyword = "on";
    langague = "en";
    when(productRepository.searchListedByCriteria(any(), any(Pageable.class))).thenReturn(
        mockResultReturn);

    var result = productService.findProducts(type, keyword, langague, simplePageable);
    assertEquals(result, mockResultReturn);
    verify(productRepository).searchListedByCriteria(any(), any(Pageable.class));
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

  private void mockMarketRepoMetaStatus() {
    var mockMartketRepoMeta = new GitHubRepoMeta();
    mockMartketRepoMeta.setRepoURL(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setLastChange(LAST_CHANGE_TIME);
    mockMartketRepoMeta.setLastSHA1(SHA1_SAMPLE);
    when(repoMetaRepository.findByRepoName(any())).thenReturn(mockMartketRepoMeta);
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
