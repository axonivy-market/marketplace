package com.axonivy.market.service;

import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
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

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private static final String SAMPLE_PRODUCT_NAME = "amazon-comprehend";
  private static final long LAST_CHANGE_TIME = 1718096290000l;
  private static final Pageable PAGEABLE = PageRequest.of(0, 20);
  private static final String SHA1_SAMPLE = "35baa89091b2452b77705da227f1a964ecabc6c8";
  private String keyword;
  private Page<Product> mockResultReturn;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private GHAxonIvyMarketRepoService marketRepoService;

  @Mock
  private GithubRepoMetaRepository repoMetaRepository;

  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  public void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindProducts() {
    // By All
    when(productRepository.findAll(PAGEABLE)).thenReturn(mockResultReturn);
    mockMarketRepoMetaStatus();
    var result = productService.findProducts(FilterType.ALL.getOption(), keyword, PAGEABLE);
    Assertions.assertEquals(mockResultReturn, result);
    verify(productRepository).findAll(PAGEABLE);

    // By connectors
    var mockCommit = mock(GHCommit.class);
    var endSHA1 = SHA1_SAMPLE.concat("end");
    when(mockCommit.getSHA1()).thenReturn(endSHA1);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    var typeCode = FilterType.CONNECTORS.getCode();
    var pagedResult = new PageImpl<Product>(
        mockResultReturn.filter(product -> product.getType().equals(typeCode)).toList());
    when(productRepository.findByType(typeCode, PAGEABLE)).thenReturn(pagedResult);
    var mockGithubFile = new GitHubFile();
    mockGithubFile.setFileName(META_FILE);
    mockGithubFile.setType(FileType.META);
    mockGithubFile.setStatus(FileStatus.ADDED);
    when(marketRepoService.fetchMarketItemsBySHA1Range(anyString(), anyString())).thenReturn(List.of(mockGithubFile));
    var mockGHContent = mock(GHContent.class);
    when(mockGHContent.getName()).thenReturn(META_FILE);
    when(marketRepoService.getGHContent(anyString())).thenReturn(mockGHContent);

    result = productService.findProducts(FilterType.CONNECTORS.getOption(), keyword, PAGEABLE);
    Assertions.assertEquals(pagedResult, result);
  }

  @Test
  void testFindAllProductsFirstTime() throws IOException {
    String type = FilterType.ALL.getOption();
    when(productRepository.findAll(PAGEABLE)).thenReturn(mockResultReturn);
    var mockCommit = mock(GHCommit.class);
    when(mockCommit.getSHA1()).thenReturn(SHA1_SAMPLE);
    when(marketRepoService.getLastCommit(anyLong())).thenReturn(mockCommit);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(null);

    var mockContent = mock(GHContent.class);
    when(mockContent.getName()).thenReturn(META_FILE);
    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(mockContent.read()).thenReturn(inputStream);
    Map<String, List<GHContent>> mockGHContentMap = new HashMap<>();
    mockGHContentMap.put(SAMPLE_PRODUCT_NAME, List.of(mockContent));
    when(marketRepoService.fetchAllMarketItems()).thenReturn(mockGHContentMap);

    var result = productService.findProducts(type, keyword, PAGEABLE);
    Assertions.assertEquals(result, mockResultReturn);
    verify(productRepository).findAll(PAGEABLE);
  }

  @Test
  void testSearchProducts() {
    String type = FilterType.ALL.getOption();
    keyword = "on";
    when(productRepository.searchByNameOrShortDescriptionRegex(keyword, PAGEABLE)).thenReturn(mockResultReturn);

    var result = productService.findProducts(type, keyword, PAGEABLE);
    Assertions.assertEquals(result, mockResultReturn);
    verify(productRepository).searchByNameOrShortDescriptionRegex(keyword, PAGEABLE);
  }

  private Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_NAME);
    mockProduct.setName("Amazon Comprehend");
    mockProduct.setType("connector");
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    mockProduct.setName("Swiss phone directory");
    mockProduct.setType("util");
    mockProducts.add(mockProduct);

    return new PageImpl<Product>(mockProducts);
  }

  private void mockMarketRepoMetaStatus() {
    var mockMartketRepoMeta = new GithubRepoMeta();
    mockMartketRepoMeta.setRepoURL(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setLastChange(LAST_CHANGE_TIME);
    mockMartketRepoMeta.setLastSHA1(SHA1_SAMPLE);
    when(repoMetaRepository.findByRepoName(anyString())).thenReturn(mockMartketRepoMeta);
  }
}
