package com.axonivy.market.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.FilterType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.util.GithubUtils;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

import static org.apache.commons.lang3.StringUtils.*;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final GHAxonIvyMarketRepoService githubMarketRepoService;
  private final GithubRepoMetaRepository repoMetaRepository;
  private GHCommit lastGHCommit;
  private GithubRepoMeta marketRepoMeta;

  public ProductServiceImpl(ProductRepository productRepository, GHAxonIvyMarketRepoService githubService,
      GithubRepoMetaRepository repoMetaRepository) {
    this.productRepository = productRepository;
    this.githubMarketRepoService = githubService;
    this.repoMetaRepository = repoMetaRepository;
  }

  @Override
  public Page<Product> findProducts(String type, String keyword, Pageable pageable) {
    final FilterType filterType = FilterType.of(type);
    if (StringUtils.isNoneBlank(keyword)) {
      return searchProducts(filterType, keyword, pageable);
    }

    if (!isLastGithubCommitCovered()) {
      if (marketRepoMeta == null) {
        syncProductsFromGithubRepo();
        marketRepoMeta = new GithubRepoMeta();
      } else {
        updateLatestChangeToProductsFromGithubRepo();
      }
      syncRepoMetaDataStatus();
    }

    Pageable unifiedPageabe = refinePagination(pageable);
    return switch (filterType) {
    case ALL -> productRepository.findAll(unifiedPageabe);
    case CONNECTORS, UTILITIES, SOLUTIONS -> productRepository.findByType(filterType.getCode(), pageable);
    default -> Page.empty();
    };
  }

  private void syncRepoMetaDataStatus() {
    if (marketRepoMeta == null || lastGHCommit == null) {
      return;
    }
    marketRepoMeta.setRepoURL(lastGHCommit.getOwner().getUrl().getPath());
    marketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    marketRepoMeta.setLastSHA1(lastGHCommit.getSHA1());
    marketRepoMeta.setLastChange(GithubUtils.getGHCommitDate(lastGHCommit));
    repoMetaRepository.save(marketRepoMeta);
    marketRepoMeta = null;
  }

  private void updateLatestChangeToProductsFromGithubRepo() {
    if (lastGHCommit == null || marketRepoMeta == null) {
      return;
    }
    var githubFileChanges = githubMarketRepoService.fetchMarketItemsBySHA1Range(marketRepoMeta.getLastSHA1(),
        lastGHCommit.getSHA1());
    Map<String, List<GitHubFile>> groupGithubFiles = new HashMap<>();
    for (var file : githubFileChanges) {
      var filePath = file.getFileName();
      var parentPath = filePath.replace(FileType.META.getFileName(), EMPTY).replace(FileType.LOGO.getFileName(), EMPTY);
      var files = groupGithubFiles.getOrDefault(parentPath, new ArrayList<>());
      files.add(file);
      groupGithubFiles.putIfAbsent(parentPath, files);
    }

    for (var parentPath : groupGithubFiles.keySet()) {
      var files = groupGithubFiles.get(parentPath);
      for (var file : files) {
        Product product = new Product();
        var fileContent = githubMarketRepoService.getGHContent(file.getFileName());
        ProductFactory.mappingByGHContent(product, fileContent);
        if (FileType.META == file.getType()) {
          modifyProductByMetaContent(file, product);
        } else {
          modifyProductLogo(parentPath, file, product, fileContent);
        }
      }
    }
  }

  private void modifyProductLogo(String parentPath, GitHubFile file, Product product, GHContent fileContent) {
    Product result = null;
    switch (file.getStatus()) {
    case MODIFIED, ADDED:
      result = productRepository.findByMarketDirectoryRegex(parentPath);
      if (result != null) {
        result.setLogoUrl(GithubUtils.getDownloadUrl(fileContent));
        productRepository.save(result);
      }
      break;
    case REMOVED:
      result = productRepository.findByLogoUrl(product.getLogoUrl());
      if (result != null) {
        productRepository.deleteById(result.getKey());
      }
      break;
    default:
      break;
    }
  }

  private void modifyProductByMetaContent(GitHubFile file, Product product) {
    switch (file.getStatus()) {
    case MODIFIED, ADDED:
      productRepository.save(product);
      break;
    case REMOVED:
      productRepository.deleteById(product.getKey());
      break;
    default:
      break;
    }
  }

  private Pageable refinePagination(Pageable pageable) {
    PageRequest pageRequest = (PageRequest) pageable;
    if (pageable != null && pageable.getSort() != null) {
      List<Order> orders = new ArrayList<Sort.Order>();
      for (var sort : pageable.getSort()) {
        final SortOption sortOption = SortOption.of(sort.getProperty());
        Order order = new Order(sort.getDirection(), sortOption.getCode());
        orders.add(order);
      }
      pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
    return pageRequest;
  }

  private boolean isLastGithubCommitCovered() {
    boolean isLastCommitCovered = false;
    long lastCommitTime = 0l;
    marketRepoMeta = repoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    if (marketRepoMeta != null) {
      lastCommitTime = marketRepoMeta.getLastChange();
    }
    lastGHCommit = githubMarketRepoService.getLastCommit(lastCommitTime);
    if (lastGHCommit != null && marketRepoMeta != null && lastGHCommit.getSHA1().equals(marketRepoMeta.getLastSHA1())) {
      isLastCommitCovered = true;
    }
    return isLastCommitCovered;
  }

  private Page<Product> syncProductsFromGithubRepo() {
    var githubContentMap = githubMarketRepoService.fetchAllMarketItems();
    List<Product> products = new ArrayList<>();
    for (var contentKey : githubContentMap.keySet()) {
      Product product = new Product();
      for (var content : githubContentMap.get(contentKey)) {
        ProductFactory.mappingByGHContent(product, content);
      }
      products.add(product);
    }
    productRepository.saveAll(products);
    return new PageImpl<Product>(products);
  }

  public Page<Product> searchProducts(FilterType filterType, String keyword, Pageable pageable) {
    Pageable unifiedPageabe = refinePagination(pageable);
    if (StringUtils.isBlank(keyword)) {
      return productRepository.findAll(pageable);
    }
    return productRepository.searchByKeywordAndType(keyword, filterType.getCode(), unifiedPageabe);
  }
}
