package com.axonivy.market.service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.MultilingualismValue;
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  private static final String SAMPLE_PRODUCT_NAME = "Amazon Comprehend";
  private static final long LAST_CHANGE_TIME = 1718096290000l;
  private static final Integer SAMPLE_PRODUCT_ONE_STAR_COUNT = 5;
  private static final Integer SAMPLE_PRODUCT_TWO_STAR_COUNT = 10;
  private static final Integer SAMPLE_PRODUCT_THREE_STAR_COUNT = 15;
  private static final Integer SAMPLE_PRODUCT_FOUR_STAR_COUNT = 20;
  private static final Integer SAMPLE_PRODUCT_FIVE_STAR_COUNT = 50;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  private String keyword;
  private String langague;
  private Page<Product> mockResultReturn;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private GHAxonIvyMarketRepoService marketRepoService;

  @Mock
  private GitHubRepoMetaRepository repoMetaRepository;

  @Mock
  private GitHubService gitHubService;

  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  public void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindProducts() {
    langague = "en";
    // Start testing by All
    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, langague, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Connector
    when(productRepository.findByType(any(), any(Pageable.class))).thenReturn(mockResultReturn);
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
    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, langague, PAGEABLE);
    assertEquals(mockResultReturn, result);
    verify(productRepository).findAll(any(Pageable.class));

    // Test has keyword
    when(productRepository.searchByNameOrShortDescriptionRegex(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getNames().getEn().equals(SAMPLE_PRODUCT_NAME)).collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, langague, PAGEABLE);
    verify(productRepository).findAll(any(Pageable.class));
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().getEn());

    // Test has keyword and type is connector
    when(productRepository.searchByKeywordAndType(any(), any(), any(), any(Pageable.class))).thenReturn(
        new PageImpl<>(mockResultReturn.stream().filter(product -> product.getNames().getEn().equals(SAMPLE_PRODUCT_NAME)
            && product.getType().equals(TypeOption.CONNECTORS.getCode())).collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, langague, PAGEABLE);
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().getEn());
  }

  @Test
  void testSyncProductsFirstTime() throws IOException {
    var mockCommit = mockGHCommitHasSHA1(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

    var mockContent = mockGHContentAsMetaJSON();
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);

    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_ID, List.of(mockContent));
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertEquals(false, result);
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
    when(productRepository.searchByNameOrShortDescriptionRegex(keyword, langague, simplePageable)).thenReturn(mockResultReturn);

    var result = productService.findProducts(type, keyword, langague, simplePageable);
    assertEquals(result, mockResultReturn);
    verify(productRepository).searchByNameOrShortDescriptionRegex(keyword, langague, simplePageable);
  }

  private Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    MultilingualismValue name = new MultilingualismValue();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    name.setEn(SAMPLE_PRODUCT_NAME);
    mockProduct.setNames(name);
    mockProduct.setType("connector");
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    name = new MultilingualismValue();
    name.setEn("Swiss phone directory");
    mockProduct.setNames(name);
    mockProduct.setType("util");
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }

  private Product createProductMock() {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    return mockProduct;
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
}
