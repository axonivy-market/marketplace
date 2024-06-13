package com.axonivy.market.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private long lastChangeTime = 1718096290000l;
  private Pageable pageable = PageRequest.of(0, 20);
  private String keyword;
  private Page<Product> mockResultReturn;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private GHAxonIvyMarketRepoService githubService;

  @Mock
  private GithubRepoMetaRepository repoMetaRepository;

  @InjectMocks
  private ProductServiceImpl productService;

  @BeforeEach
  public void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindAllProducts() {
    when(productRepository.findAll(pageable)).thenReturn(mockResultReturn);
    mockMarketRepoMetaStatus();
    Page<Product> result = productService.findProducts(FilterType.ALL.getOption(), keyword, pageable);
    Assertions.assertEquals(mockResultReturn, result);
    verify(productRepository).findAll(pageable);
  }

  @Test
  void testFindAllProductsFirstTime() {
    String type = FilterType.ALL.getOption();
    when(productRepository.findAll(pageable)).thenReturn(mockResultReturn);
    when(githubService.getLastCommit(lastChangeTime)).thenReturn(new GHCommit());
    mockMarketRepoMetaStatus();

    Page<Product> result = productService.findProducts(type, keyword, pageable);
    Assertions.assertEquals(result, mockResultReturn);
    verify(productRepository).findAll(pageable);
  }

  @Test
  void testSearchProducts() {
    String type = FilterType.ALL.getOption();
    keyword = "o";
    when(productRepository.searchByNameOrShortDescriptionRegex(keyword, pageable)).thenReturn(mockResultReturn);

    Page<Product> result = productService.findProducts(type, keyword, pageable);
    Assertions.assertEquals(result, mockResultReturn);
    verify(productRepository).searchByNameOrShortDescriptionRegex(keyword, pageable);
  }

  private Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Product mockProduct = new Product();
    mockProduct.setId("amazon-comprehend");
    mockProduct.setName("Amazon Comprehend");
    mockProduct.setType("connector");
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    mockProduct.setName("Swiss phone directory");
    mockProduct.setType("connector");
    mockProducts.add(mockProduct);

    return new PageImpl<Product>(mockProducts);
  }

  private void mockMarketRepoMetaStatus() {
    var mockMartketRepoMeta = new GithubRepoMeta();
    mockMartketRepoMeta.setRepoURL("/repos/axonivy-market/market");
    mockMartketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    mockMartketRepoMeta.setLastChange(lastChangeTime);
    mockMartketRepoMeta.setLastSHA1("35baa89091b2452b77705da227f1a964ecabc6c8");
    Mockito.when(repoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME))
        .thenReturn(mockMartketRepoMeta);
  }
}
