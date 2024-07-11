package com.axonivy.market.service.impl;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.entity.ProductModuleContent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final GHAxonIvyProductRepoService axonIvyProductRepoService;
  private final GitHubRepoMetaRepository gitHubRepoMetaRepository;
  private final GitHubService gitHubService;

  private GHCommit lastGHCommit;
  private GitHubRepoMeta marketRepoMeta;

  public static final String NON_NUMERIC_CHAR = "[^0-9.]";

  public ProductServiceImpl(ProductRepository productRepository, GHAxonIvyMarketRepoService axonIvyMarketRepoService,
      GHAxonIvyProductRepoService axonIvyProductRepoService, GitHubRepoMetaRepository gitHubRepoMetaRepository,
      GitHubService gitHubService) {
    this.productRepository = productRepository;
    this.axonIvyMarketRepoService = axonIvyMarketRepoService;
    this.axonIvyProductRepoService = axonIvyProductRepoService;
    this.gitHubRepoMetaRepository = gitHubRepoMetaRepository;
    this.gitHubService = gitHubService;
  }

  @Override
  public Page<Product> findProducts(String type, String keyword, String language, Pageable pageable) {
    final var typeOption = TypeOption.of(type);
    final var searchPageable = refinePagination(language, pageable);
    Page<Product> result = Page.empty();
    switch (typeOption) {
      case ALL:
        if (StringUtils.isBlank(keyword)) {
          result = productRepository.findAll(searchPageable);
        } else {
          result = productRepository.searchByNameOrShortDescriptionRegex(keyword, language, searchPageable);
        }
        break;
      case CONNECTORS, UTILITIES, SOLUTIONS:
        if (StringUtils.isBlank(keyword)) {
          result = productRepository.findByType(typeOption.getCode(), searchPageable);
        } else {
          result = productRepository.searchByKeywordAndType(keyword, typeOption.getCode(), language, searchPageable);
        }
        break;
      default:
        break;
    }
    return result;
  }

  @Override
  public boolean syncLatestDataFromMarketRepo() {
    var isAlreadyUpToDate = isLastGithubCommitCovered();
    if (!isAlreadyUpToDate) {
      if (marketRepoMeta == null) {
        syncProductsFromGitHubRepo();
        marketRepoMeta = new GitHubRepoMeta();
      } else {
        updateLatestChangeToProductsFromGithubRepo();
      }
      syncRepoMetaDataStatus();
    }
    return isAlreadyUpToDate;
  }

  private void syncRepoMetaDataStatus() {
    if (lastGHCommit == null) {
      return;
    }
    String repoURL =
        Optional.ofNullable(lastGHCommit.getOwner()).map(GHRepository::getUrl).map(URL::getPath).orElse(EMPTY);
    marketRepoMeta.setRepoURL(repoURL);
    marketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    marketRepoMeta.setLastSHA1(lastGHCommit.getSHA1());
    marketRepoMeta.setLastChange(GitHubUtils.getGHCommitDate(lastGHCommit));
    gitHubRepoMetaRepository.save(marketRepoMeta);
    marketRepoMeta = null;
  }

  private void updateLatestChangeToProductsFromGithubRepo() {
    var fromSHA1 = marketRepoMeta.getLastSHA1();
    var toSHA1 = ofNullable(lastGHCommit).map(GHCommit::getSHA1).orElse("");
    log.warn("**ProductService: synchronize products from SHA1 {} to SHA1 {}", fromSHA1, toSHA1);
    List<GitHubFile> gitHubFileChanges = axonIvyMarketRepoService.fetchMarketItemsBySHA1Range(fromSHA1, toSHA1);
    Map<String, List<GitHubFile>> groupGitHubFiles = new HashMap<>();
    for (var file : gitHubFileChanges) {
      String filePath = file.getFileName();
      var parentPath = filePath.replace(FileType.META.getFileName(), EMPTY).replace(FileType.LOGO.getFileName(), EMPTY);
      var files = groupGitHubFiles.getOrDefault(parentPath, new ArrayList<>());
      files.add(file);
      groupGitHubFiles.putIfAbsent(parentPath, files);
    }

    groupGitHubFiles.entrySet().forEach(ghFileEntity -> {
      for (var file : ghFileEntity.getValue()) {
        Product product = new Product();
        GHContent fileContent;
        try {
          fileContent = gitHubService.getGHContent(axonIvyMarketRepoService.getRepository(), file.getFileName(),
              GitHubConstants.DEFAULT_BRANCH);
        } catch (IOException e) {
          log.error("Get GHContent failed: ", e);
          continue;
        }

        ProductFactory.mappingByGHContent(product, fileContent);
        updateProductFromReleaseTags(product);
        if (FileType.META == file.getType()) {
          modifyProductByMetaContent(file, product);
        } else {
          modifyProductLogo(ghFileEntity.getKey(), file, product, fileContent);
        }
      }
    });
  }

  private void modifyProductLogo(String parentPath, GitHubFile file, Product product, GHContent fileContent) {
    Product result = null;
    switch (file.getStatus()) {
      case MODIFIED, ADDED:
        result = productRepository.findByMarketDirectoryRegex(parentPath);
        if (result != null) {
          result.setLogoUrl(GitHubUtils.getDownloadUrl(fileContent));
          productRepository.save(result);
        }
        break;
      case REMOVED:
        result = productRepository.findByLogoUrl(product.getLogoUrl());
        if (result != null) {
          productRepository.deleteById(result.getId());
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
        productRepository.deleteById(product.getId());
        break;
      default:
        break;
    }
  }

  private Pageable refinePagination(String language, Pageable pageable) {
    PageRequest pageRequest = (PageRequest) pageable;
    if (pageable != null) {
      List<Order> orders = new ArrayList<>();
      for (var sort : pageable.getSort()) {
        final var sortOption = SortOption.of(sort.getProperty());
        Order order = new Order(sort.getDirection(), sortOption.getCode(language));
        orders.add(order);
      }
      pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
    return pageRequest;
  }

  private boolean isLastGithubCommitCovered() {
    boolean isLastCommitCovered = false;
    long lastCommitTime = 0l;
    marketRepoMeta = gitHubRepoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    if (marketRepoMeta != null) {
      lastCommitTime = marketRepoMeta.getLastChange();
    }
    lastGHCommit = axonIvyMarketRepoService.getLastCommit(lastCommitTime);
    if (lastGHCommit != null && marketRepoMeta != null
        && StringUtils.equals(lastGHCommit.getSHA1(), marketRepoMeta.getLastSHA1())) {
      isLastCommitCovered = true;
    }
    return isLastCommitCovered;
  }

  private Page<Product> syncProductsFromGitHubRepo() {
    log.warn("**ProductService: synchronize products from scratch based on the Market repo");
    var gitHubContentMap = axonIvyMarketRepoService.fetchAllMarketItems();
    List<Product> products = new ArrayList<>();
    gitHubContentMap.entrySet().forEach(ghContentEntity -> {
      Product product = new Product();
      for (var content : ghContentEntity.getValue()) {
        ProductFactory.mappingByGHContent(product, content);
        updateProductFromReleaseTags(product);
      }
      products.add(product);
    });
    if (!products.isEmpty()) {
      productRepository.saveAll(products);
    }
    return new PageImpl<>(products);
  }

  private void updateProductFromReleaseTags(Product product) {
    if (StringUtils.isBlank(product.getRepositoryName())) {
      return;
    }
    try {
      GHRepository productRepo = gitHubService.getRepository(product.getRepositoryName());
      List<GHTag> tags = productRepo.listTags().toList();
      GHTag lastTag = CollectionUtils.firstElement(tags);
      if (lastTag != null) {
        product.setNewestPublishedDate(lastTag.getCommit().getCommitDate());
        product.setNewestReleaseVersion(lastTag.getName());
      }

      String oldestTag = tags.stream().map(tag -> tag.getName().replaceAll(NON_NUMERIC_CHAR, Strings.EMPTY)).distinct()
          .sorted(Comparator.reverseOrder()).reduce((tag1, tag2) -> tag2).orElse(null);
      if (oldestTag != null && StringUtils.isBlank(product.getCompatibility())) {
        String compatibility = getCompatibilityFromOldestTag(oldestTag);
        product.setCompatibility(compatibility);
      }

      List<CompletableFuture<ProductModuleContent>> completableFutures = new ArrayList<>();
      ExecutorService service = Executors.newFixedThreadPool(10);
      for (GHTag ghtag : tags) {
        completableFutures.add(CompletableFuture.supplyAsync(
            () -> axonIvyProductRepoService.getReadmeAndProductContentsFromTag(product, productRepo, ghtag.getName()),
            service));
      }
      completableFutures.forEach(CompletableFuture::join);
      List<ProductModuleContent> productModuleContents = completableFutures.stream().map(completableFuture -> {
        try {
          return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
          Thread.currentThread().interrupt();
          log.error("Get readme and product json contents failed", e);
          return null;
        }
      }).toList();
      product.setProductModuleContents(productModuleContents);
    } catch (Exception e) {
      log.error("Cannot find repository by path {} {}", product.getRepositoryName(), e);
    }
  }

  // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
  public String getCompatibilityFromOldestTag(String oldestTag) {
    if (!oldestTag.contains(CommonConstants.DOT_SEPARATOR)) {
      return oldestTag + ".0+";
    }
    int firstDot = oldestTag.indexOf(CommonConstants.DOT_SEPARATOR);
    int secondDot = oldestTag.indexOf(CommonConstants.DOT_SEPARATOR, firstDot + 1);
    if (secondDot == -1) {
      return oldestTag.concat(CommonConstants.PLUS);
    }
    return oldestTag.substring(0, secondDot).concat(CommonConstants.PLUS);
  }

  @Override
  public Product fetchProductDetail(String id) {
    return productRepository.findById(id).orElse(null);
  }
}
