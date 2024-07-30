package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final GHAxonIvyProductRepoService axonIvyProductRepoService;
  private final GitHubRepoMetaRepository gitHubRepoMetaRepository;
  private final GitHubService gitHubService;
  private final ProductCustomSortRepository productCustomSortRepository;

  private final MongoTemplate mongoTemplate;

  private GHCommit lastGHCommit;
  private GitHubRepoMeta marketRepoMeta;
  private final ObjectMapper mapper = new ObjectMapper();

  @Value("${synchronized.installation.counts.path}")
  private String installationCountPath;

  public static final String NON_NUMERIC_CHAR = "[^0-9.]";
  private final SecureRandom random = new SecureRandom();

  public ProductServiceImpl(ProductRepository productRepository, GHAxonIvyMarketRepoService axonIvyMarketRepoService,
      GHAxonIvyProductRepoService axonIvyProductRepoService, GitHubRepoMetaRepository gitHubRepoMetaRepository,
      GitHubService gitHubService, ProductCustomSortRepository productCustomSortRepository,
      MongoTemplate mongoTemplate) {
    this.productRepository = productRepository;
    this.axonIvyMarketRepoService = axonIvyMarketRepoService;
    this.axonIvyProductRepoService = axonIvyProductRepoService;
    this.gitHubRepoMetaRepository = gitHubRepoMetaRepository;
    this.gitHubService = gitHubService;
    this.productCustomSortRepository = productCustomSortRepository;
    this.mongoTemplate = mongoTemplate;
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

  @Override
  public int updateInstallationCountForProduct(String key) {
    return productRepository.findById(key).map(product -> {
      log.info("updating installation count for product {}", key);
      if (!BooleanUtils.isTrue(product.getSynchronizedInstallationCount())) {
        syncInstallationCountWithProduct(product);
      }
      product.setInstallationCount(product.getInstallationCount() + 1);
      return productRepository.save(product);
    }).map(Product::getInstallationCount).orElse(0);
  }

  private void syncInstallationCountWithProduct(Product product) {
    log.info("synchronizing installation count for product {}", product.getId());
    try {
      String installationCounts = Files.readString(Paths.get(installationCountPath));
      Map<String, Integer> mapping = mapper.readValue(installationCounts,
          new TypeReference<HashMap<String, Integer>>() {
          });
      List<String> keyList = mapping.keySet().stream().toList();
      int currentInstallationCount = keyList.contains(product.getId())
          ? mapping.get(product.getId())
          : random.nextInt(20, 50);
      product.setInstallationCount(currentInstallationCount);
      product.setSynchronizedInstallationCount(true);
      log.info("synchronized installation count for product {} successfully", product.getId());
    } catch (IOException ex) {
      log.error(ex.getMessage());
      log.error("Could not read the marketplace-installation file to synchronize");
    }
  }

  private void syncRepoMetaDataStatus() {
    if (lastGHCommit == null) {
      return;
    }
    String repoURL = Optional.ofNullable(lastGHCommit.getOwner()).map(GHRepository::getUrl).map(URL::getPath)
        .orElse(EMPTY);
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
        SortOption sortOption = SortOption.of(sort.getProperty());
        Order order = createOrder(sortOption, language);
        orders.add(order);
        if (SortOption.STANDARD.equals(sortOption)) {
          orders.add(getExtensionOrder(language));
        }
      }
      pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
    return pageRequest;
  }

  public Order createOrder(SortOption sortOption, String language) {
    return new Order(sortOption.getDirection(), sortOption.getCode(language));
  }

  private Order getExtensionOrder(String language) {
    List<ProductCustomSort> customSorts = productCustomSortRepository.findAll();

    if (!customSorts.isEmpty()) {
      SortOption sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      return createOrder(sortOptionExtension, language);
    }
    return createOrder(SortOption.POPULARITY, language);
  }

  private boolean isLastGithubCommitCovered() {
    boolean isLastCommitCovered = false;
    long lastCommitTime = 0L;
    marketRepoMeta = gitHubRepoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    if (marketRepoMeta != null) {
      lastCommitTime = marketRepoMeta.getLastChange();
    }
    lastGHCommit = axonIvyMarketRepoService.getLastCommit(lastCommitTime);
    if (lastGHCommit != null && marketRepoMeta != null && StringUtils.equals(lastGHCommit.getSHA1(),
        marketRepoMeta.getLastSHA1())) {
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

      List<ProductModuleContent> productModuleContents = new ArrayList<>();
      for (GHTag ghtag : tags) {
        ProductModuleContent productModuleContent = axonIvyProductRepoService.getReadmeAndProductContentsFromTag(
            product, productRepo, ghtag.getName());
        productModuleContents.add(productModuleContent);
      }
      product.setProductModuleContents(productModuleContents);
    } catch (Exception e) {
      log.error("Cannot find repository by path {} {}", product.getRepositoryName(), e);
    }
  }

  // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
  @Override
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
    Product product = productRepository.findById(id).orElse(null);
    return Optional.ofNullable(product).map(productItem -> {
      if (!BooleanUtils.isTrue(productItem.getSynchronizedInstallationCount())) {
        syncInstallationCountWithProduct(productItem);
        return productRepository.save(productItem);
      }
      return productItem;
    }).orElse(null);
  }

  @Override
  public void clearAllProducts() {
    gitHubRepoMetaRepository.deleteAll();
    productRepository.deleteAll();
  }

  @Override
  public void addCustomSortProduct(ProductCustomSortRequest customSort) throws InvalidParamException {
    SortOption.of(customSort.getRuleForRemainder());

    ProductCustomSort productCustomSort = new ProductCustomSort(customSort.getRuleForRemainder());
    productCustomSortRepository.deleteAll();
    removeFieldFromAllProductDocuments(ProductJsonConstants.CUSTOM_ORDER);
    productCustomSortRepository.save(productCustomSort);
    productRepository.saveAll(refineOrderedListOfProductsInCustomSort(customSort.getOrderedListOfProducts()));
  }

  public List<Product> refineOrderedListOfProductsInCustomSort(List<String> orderedListOfProducts)
      throws InvalidParamException {
    List<Product> productEntries = new ArrayList<>();

    int descendingOrder = orderedListOfProducts.size();
    for (String productId : orderedListOfProducts) {
      Optional<Product> productOptional = productRepository.findById(productId);

      if (productOptional.isEmpty()) {
        throw new InvalidParamException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
      }
      Product product = productOptional.get();
      product.setCustomOrder(descendingOrder--);
      productEntries.add(product);
    }

    return productEntries;
  }

  public void removeFieldFromAllProductDocuments(String fieldName) {
    Update update = new Update().unset(fieldName);
    mongoTemplate.updateMulti(new Query(), update, Product.class);
  }
}
