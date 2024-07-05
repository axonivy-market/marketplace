package com.axonivy.market.service;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.ProductRating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  private static final String SAMPLE_PRODUCT_NAME = "Amazon Comprehend";
  private static final Integer SAMPLE_PRODUCT_ONE_STAR_COUNT = 5;
  private static final Integer SAMPLE_PRODUCT_TWO_STAR_COUNT = 10;
  private static final Integer SAMPLE_PRODUCT_THREE_STAR_COUNT = 15;
  private static final Integer SAMPLE_PRODUCT_FOUR_STAR_COUNT = 20;
  private static final Integer SAMPLE_PRODUCT_FIVE_STAR_COUNT = 50;
  private static final long LAST_CHANGE_TIME = 1718096290000L;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  private String keyword;
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
    // Start testing by All
    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Connector
    when(productRepository.findByType(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, PAGEABLE);
    assertEquals(mockResultReturn, result);

    // Start testing by Other
    // Executes
    result = productService.findProducts(TypeOption.DEMOS.getOption(), keyword, PAGEABLE);
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
    when(gitHubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);

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
    when(gitHubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);

    // Executes
    var result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);

    // Start testing by deleting new logo
    when(mockCommit.getSHA1()).thenReturn(UUID.randomUUID().toString());
    mockGitHubFile.setStatus(FileStatus.REMOVED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(any(), any())).thenReturn(List.of(mockGitHubFile));
    when(gitHubService.getGHContent(any(), anyString())).thenReturn(mockGHContent);
    when(productRepository.findByLogoUrl(any())).thenReturn(new Product());

    // Executes
    result = productService.syncLatestDataFromMarketRepo();
    assertFalse(result);
  }

  @Test
  void testFindAllProductsWithKeyword() {
    when(productRepository.findAll(any(Pageable.class))).thenReturn(mockResultReturn);
    // Executes
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, PAGEABLE);
    assertEquals(mockResultReturn, result);
    verify(productRepository).findAll(any(Pageable.class));

    // Test has keyword
    when(productRepository.searchByNameOrShortDescriptionRegex(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(mockResultReturn.stream()
            .filter(product -> product.getName().equals(SAMPLE_PRODUCT_NAME)).collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, PAGEABLE);
    verify(productRepository).findAll(any(Pageable.class));
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getName());

    // Test has keyword and type is connector
    when(productRepository.searchByKeywordAndType(any(), any(), any(Pageable.class))).thenReturn(
        new PageImpl<>(mockResultReturn.stream().filter(product -> product.getName().equals(SAMPLE_PRODUCT_NAME)
            && product.getType().equals(TypeOption.CONNECTORS.getCode())).collect(Collectors.toList())));
    // Executes
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, PAGEABLE);
    assertTrue(result.hasContent());
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getName());
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
    assertFalse(result);
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
    when(productRepository.searchByNameOrShortDescriptionRegex(keyword, simplePageable)).thenReturn(mockResultReturn);

    var result = productService.findProducts(type, keyword, simplePageable);
    assertEquals(result, mockResultReturn);
    verify(productRepository).searchByNameOrShortDescriptionRegex(keyword, simplePageable);
  }

  @Test
  void testGetProductRatingById() {
    when(productRepository.findById(SAMPLE_PRODUCT_ID)).thenReturn(java.util.Optional.of(createProductMock()));
    // Executes
    List<ProductRating> result = productService.getProductRatingById(SAMPLE_PRODUCT_ID);

    // Assertions
    assertEquals(5, result.size());
    assertEquals(1, result.get(0).getStarRating());
    assertEquals(SAMPLE_PRODUCT_ONE_STAR_COUNT, result.get(0).getCommentNumber());
    assertEquals(calculatePercentage(SAMPLE_PRODUCT_ONE_STAR_COUNT), result.get(0).getPercent());

    assertEquals(2, result.get(1).getStarRating());
    assertEquals(SAMPLE_PRODUCT_TWO_STAR_COUNT, result.get(1).getCommentNumber());
    assertEquals(calculatePercentage(SAMPLE_PRODUCT_TWO_STAR_COUNT), result.get(1).getPercent());

    assertEquals(3, result.get(2).getStarRating());
    assertEquals(SAMPLE_PRODUCT_THREE_STAR_COUNT, result.get(2).getCommentNumber());
    assertEquals(calculatePercentage(SAMPLE_PRODUCT_THREE_STAR_COUNT), result.get(2).getPercent());

    assertEquals(4, result.get(3).getStarRating());
    assertEquals(SAMPLE_PRODUCT_FOUR_STAR_COUNT, result.get(3).getCommentNumber());
    assertEquals(calculatePercentage(SAMPLE_PRODUCT_FOUR_STAR_COUNT), result.get(3).getPercent());

    assertEquals(5, result.get(4).getStarRating());
    assertEquals(SAMPLE_PRODUCT_FIVE_STAR_COUNT, result.get(4).getCommentNumber());
    assertEquals(calculatePercentage(SAMPLE_PRODUCT_FIVE_STAR_COUNT), result.get(4).getPercent());
  }

  @Test
  void testGetProductRatingById_ProductNotFound() {
    when(productRepository.findById(any())).thenReturn(java.util.Optional.empty());

    try {
      productService.getProductRatingById("non-existent-product-id");
    } catch (NotFoundException e) {
      assertEquals("PRODUCT_NOT_FOUND-Not found product with id: non-existent-product-id", e.getMessage());
    }
  }

  private int calculatePercentage(int starCount) {
    int totalComments = SAMPLE_PRODUCT_ONE_STAR_COUNT +
        SAMPLE_PRODUCT_TWO_STAR_COUNT +
        SAMPLE_PRODUCT_THREE_STAR_COUNT +
        SAMPLE_PRODUCT_FOUR_STAR_COUNT +
        SAMPLE_PRODUCT_FIVE_STAR_COUNT;

    return totalComments == 0 ? 0 : (int) ((starCount / (double) totalComments) * 100);
  }

  private Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setName(SAMPLE_PRODUCT_NAME);
    mockProduct.setOneStarCount(SAMPLE_PRODUCT_ONE_STAR_COUNT);
    mockProduct.setTwoStarCount(SAMPLE_PRODUCT_TWO_STAR_COUNT);
    mockProduct.setThreeStarCount(SAMPLE_PRODUCT_THREE_STAR_COUNT);
    mockProduct.setFourStarCount(SAMPLE_PRODUCT_FOUR_STAR_COUNT);
    mockProduct.setFiveStarCount(SAMPLE_PRODUCT_FIVE_STAR_COUNT);
    mockProduct.setType("connector");
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    mockProduct.setName("Swiss phone directory");
    mockProduct.setType("util");
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }

  private Product createProductMock() {
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    mockProduct.setName(SAMPLE_PRODUCT_NAME);
    mockProduct.setOneStarCount(SAMPLE_PRODUCT_ONE_STAR_COUNT);
    mockProduct.setTwoStarCount(SAMPLE_PRODUCT_TWO_STAR_COUNT);
    mockProduct.setThreeStarCount(SAMPLE_PRODUCT_THREE_STAR_COUNT);
    mockProduct.setFourStarCount(SAMPLE_PRODUCT_FOUR_STAR_COUNT);
    mockProduct.setFiveStarCount(SAMPLE_PRODUCT_FIVE_STAR_COUNT);
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
