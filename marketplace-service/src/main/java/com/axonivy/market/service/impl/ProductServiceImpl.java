package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Image;
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
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.GitHubRepoMetaRepository;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;
import static com.axonivy.market.enums.DocumentField.MARKET_DIRECTORY;
import static com.axonivy.market.enums.DocumentField.SHORT_DESCRIPTIONS;
import static com.axonivy.market.enums.FileStatus.ADDED;
import static com.axonivy.market.enums.FileStatus.MODIFIED;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {

  public static final String NON_NUMERIC_CHAR = "[^0-9.]";
  private static final String INITIAL_VERSION = "1.0";
  private final ProductRepository productRepository;
  private final ProductModuleContentRepository productModuleContentRepository;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final GHAxonIvyProductRepoService axonIvyProductRepoService;
  private final GitHubRepoMetaRepository gitHubRepoMetaRepository;
  private final GitHubService gitHubService;
  private final ProductCustomSortRepository productCustomSortRepository;
  private final ImageRepository imageRepository;
  private final ImageService imageService;
  private final MongoTemplate mongoTemplate;
  private final ObjectMapper mapper = new ObjectMapper();
  private final SecureRandom random = new SecureRandom();
  private GHCommit lastGHCommit;
  private GitHubRepoMeta marketRepoMeta;
  @Value("${synchronized.installation.counts.path}")
  private String installationCountPath;
  @Value("${market.github.market.branch}")
  private String marketRepoBranch;

  public ProductServiceImpl(ProductRepository productRepository,
      ProductModuleContentRepository productModuleContentRepository,
      GHAxonIvyMarketRepoService axonIvyMarketRepoService, GHAxonIvyProductRepoService axonIvyProductRepoService,
      GitHubRepoMetaRepository gitHubRepoMetaRepository, GitHubService gitHubService,
      ProductCustomSortRepository productCustomSortRepository, ImageRepository imageRepository1,
      ImageService imageService, MongoTemplate mongoTemplate) {
    this.productRepository = productRepository;
    this.productModuleContentRepository = productModuleContentRepository;
    this.axonIvyMarketRepoService = axonIvyMarketRepoService;
    this.axonIvyProductRepoService = axonIvyProductRepoService;
    this.gitHubRepoMetaRepository = gitHubRepoMetaRepository;
    this.gitHubService = gitHubService;
    this.productCustomSortRepository = productCustomSortRepository;
    this.imageRepository = imageRepository1;
    this.imageService = imageService;
    this.mongoTemplate = mongoTemplate;
  }

  private static Predicate<GHTag> filterNonPersistGhTagName(List<String> currentTags) {
    return tag -> !currentTags.contains(tag.getName());
  }

  @Override
  public Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient,
      Pageable pageable) {
    final var typeOption = TypeOption.of(type);
    final var searchPageable = refinePagination(language, pageable);
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setListed(true);
    searchCriteria.setKeyword(keyword);
    searchCriteria.setType(typeOption);
    searchCriteria.setLanguage(Language.of(language));
    if (BooleanUtils.isTrue(isRESTClient)) {
      searchCriteria.setExcludeFields(List.of(SHORT_DESCRIPTIONS));
    }
    return productRepository.searchByCriteria(searchCriteria, searchPageable);
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
    updateLatestReleaseTagContentsFromProductRepo();
    return isAlreadyUpToDate;
  }

  @Override
  public int updateInstallationCountForProduct(String key, String designerVersion) {
    Product product = productRepository.getProductById(key);
    if (Objects.isNull(product)) {
      return 0;
    }

    log.info("Increase installation count for product {} By Designer Version {}", key, designerVersion);
    if (StringUtils.isNotBlank(designerVersion)) {
      productRepository.increaseInstallationCountForProductByDesignerVersion(key, designerVersion);
    }

    log.info("updating installation count for product {}", key);
    if (BooleanUtils.isTrue(product.getSynchronizedInstallationCount())) {
      return productRepository.increaseInstallationCount(key);
    }
    syncInstallationCountWithProduct(product);
    return productRepository.updateInitialCount(key, product.getInstallationCount() + 1);
  }

  public void syncInstallationCountWithProduct(Product product) {
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
    var toSHA1 = ofNullable(lastGHCommit).map(GHCommit::getSHA1).orElse(EMPTY);
    log.warn("**ProductService: synchronize products from SHA1 {} to SHA1 {}", fromSHA1, toSHA1);
    List<GitHubFile> gitHubFileChanges = axonIvyMarketRepoService.fetchMarketItemsBySHA1Range(fromSHA1, toSHA1);
    Map<String, List<GitHubFile>> groupGitHubFiles = new HashMap<>();
    for (var file : gitHubFileChanges) {
      String filePath = file.getFileName();
      var parentPath = filePath.replace(FileType.META.getFileName(), EMPTY).replace(FileType.LOGO.getFileName(), EMPTY);
      var files = groupGitHubFiles.getOrDefault(parentPath, new ArrayList<>());
      files.add(file);
      files.sort((file1, file2) -> GitHubUtils.sortMetaJsonFirst(file1.getFileName(), file2.getFileName()));
      groupGitHubFiles.putIfAbsent(parentPath, files);
    }

    groupGitHubFiles.forEach((key, value) -> {
      for (var file : value) {
        if (file.getStatus() == MODIFIED || file.getStatus() == ADDED) {
          modifyProductMetaOrLogo(file, key);
        } else {
          removeProductAndImage(file);
        }
      }
    });
  }

  private void removeProductAndImage(GitHubFile file) {
    if (FileType.META == file.getType()) {
      String[] splitMetaJsonPath = file.getFileName().split(SLASH);
      String extractMarketDirectory = file.getFileName().replace(splitMetaJsonPath[splitMetaJsonPath.length - 1],
          EMPTY);
      List<Product> productList = productRepository.findByMarketDirectory(extractMarketDirectory);
      if (ObjectUtils.isNotEmpty(productList)) {
        String productId = productList.get(0).getId();
        productRepository.deleteById(productId);
        imageRepository.deleteAllByProductId(productId);
      }
    } else {
      List<Image> images = imageRepository.findByImageUrlEndsWithIgnoreCase(file.getFileName());
      if (ObjectUtils.isNotEmpty(images)) {
        Image currentImage = images.get(0);
        productRepository.deleteById(currentImage.getProductId());
        imageRepository.deleteAllByProductId(currentImage.getProductId());
      }
    }
  }

  private void modifyProductMetaOrLogo(GitHubFile file, String parentPath) {
    try {
      GHContent fileContent = gitHubService.getGHContent(axonIvyMarketRepoService.getRepository(), file.getFileName(),
          marketRepoBranch);
      updateProductByMetaJsonAndLogo(fileContent, file, parentPath);
    } catch (IOException e) {
      log.error("Get GHContent failed: ", e);
    }
  }

  private void updateProductByMetaJsonAndLogo(GHContent fileContent, GitHubFile file, String parentPath) {
    Product product = new Product();
    ProductFactory.mappingByGHContent(product, fileContent);
    if (FileType.META == file.getType()) {
      transferComputedDataFromDB(product);
      productRepository.save(product);
    } else {
      modifyProductLogo(parentPath, fileContent);
    }
  }

  private void modifyProductLogo(String parentPath, GHContent fileContent) {
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setKeyword(parentPath);
    searchCriteria.setFields(List.of(MARKET_DIRECTORY));
    Product result = productRepository.findByCriteria(searchCriteria);
    if (result != null) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(result, fileContent, true)).ifPresent(image -> {
        if (StringUtils.isNotBlank(result.getLogoId())) {
          imageRepository.deleteById(result.getLogoId());
        }
        result.setLogoId(image.getId());
        productRepository.save(result);
      });
    } else {
      log.info("There is no product to update the logo with path {}", parentPath);
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
      Order orderById = createOrder(SortOption.ID, language);
      orders.add(orderById);
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

  private void updateLatestReleaseTagContentsFromProductRepo() {
    List<Product> products = productRepository.findAll();
    if (ObjectUtils.isEmpty(products)) {
      return;
    }

    for (Product product : products) {
      if (StringUtils.isNotBlank(product.getRepositoryName())) {
        getProductContents(product);
        productRepository.save(product);
      }
    }
  }

  private void updateProductContentForNonStandardProduct(Map.Entry<String, List<GHContent>> ghContentEntity,
      Product product) {
    ProductModuleContent initialContent = new ProductModuleContent();
    initialContent.setTag(INITIAL_VERSION);
    initialContent.setProductId(product.getId());
    ProductFactory.mappingIdForProductModuleContent(initialContent);
    product.setReleasedVersions(List.of(INITIAL_VERSION));
    product.setNewestReleaseVersion(INITIAL_VERSION);
    axonIvyProductRepoService.extractReadMeFileFromContents(product, ghContentEntity.getValue(), initialContent);
    productModuleContentRepository.save(initialContent);
  }

  private void getProductContents(Product product) {
    try {
      GHRepository productRepo = gitHubService.getRepository(product.getRepositoryName());
      updateProductFromReleaseTags(product, productRepo);
    } catch (IOException e) {
      log.error("Cannot find product repository {} {}", product.getRepositoryName(), e);
    }
  }

  private void syncProductsFromGitHubRepo() {
    log.warn("**ProductService: synchronize products from scratch based on the Market repo");
    var gitHubContentMap = axonIvyMarketRepoService.fetchAllMarketItems();
    for (Map.Entry<String, List<GHContent>> ghContentEntity : gitHubContentMap.entrySet()) {
      Product product = new Product();
      //update the meta.json first
      ghContentEntity.getValue()
          .sort((file1, file2) -> GitHubUtils.sortMetaJsonFirst(file1.getName(), file2.getName()));
      for (var content : ghContentEntity.getValue()) {
        ProductFactory.mappingByGHContent(product, content);
        mappingLogoFromGHContent(product, content);
      }
      if (productRepository.findById(product.getId()).isPresent()) {
        continue;
      }
      if (StringUtils.isNotBlank(product.getRepositoryName())) {
        updateProductCompatibility(product);
        getProductContents(product);
      } else {
        updateProductContentForNonStandardProduct(ghContentEntity, product);
      }
      transferComputedDataFromDB(product);
      productRepository.save(product);
    }
  }

  private void mappingLogoFromGHContent(Product product, GHContent ghContent) {
    if (StringUtils.endsWith(ghContent.getName(), LOGO_FILE)) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(product, ghContent, true))
          .ifPresent(image -> product.setLogoId(image.getId()));
    }
  }

  private void updateProductFromReleaseTags(Product product, GHRepository productRepo) {
    List<ProductModuleContent> productModuleContents = new ArrayList<>();
    List<GHTag> ghTags = getProductReleaseTags(product);
    GHTag lastTag = MavenVersionComparator.findHighestTag(ghTags);
    if (lastTag == null || lastTag.getName().equals(product.getNewestReleaseVersion())) {
      return;
    }
    product.setNewestPublishedDate(getPublishedDateFromLatestTag(lastTag));
    product.setNewestReleaseVersion(lastTag.getName());
    List<String> currentTags = VersionUtils.getReleaseTagsFromProduct(product);
    if (CollectionUtils.isEmpty(currentTags)) {
      currentTags = productModuleContentRepository.findTagsByProductId(product.getId());
    }
    ghTags = ghTags.stream().filter(filterNonPersistGhTagName(currentTags)).toList();

    for (GHTag ghTag : ghTags) {
      ProductModuleContent productModuleContent =
          axonIvyProductRepoService.getReadmeAndProductContentsFromTag(product, productRepo, ghTag.getName());
      if (productModuleContent != null) {
        productModuleContents.add(productModuleContent);
      }
      String versionFromTag = VersionUtils.convertTagToVersion(ghTag.getName());
      if (Objects.isNull(product.getReleasedVersions())) {
        product.setReleasedVersions(new ArrayList<>());
      }
      product.getReleasedVersions().add(versionFromTag);
    }
    if (!CollectionUtils.isEmpty(productModuleContents)) {
      productModuleContentRepository.saveAll(productModuleContents);
    }
  }

  private Date getPublishedDateFromLatestTag(GHTag lastTag) {
    try {
      return lastTag.getCommit().getCommitDate();
    } catch (Exception e) {
      log.error("Fail to get commit date ", e);
    }
    return null;
  }

  private void updateProductCompatibility(Product product) {
    if (StringUtils.isNotBlank(product.getCompatibility())) {
      return;
    }
    String oldestVersion = VersionUtils.getOldestVersion(getProductReleaseTags(product));
    if (oldestVersion != null) {
      String compatibility = getCompatibilityFromOldestTag(oldestVersion);
      product.setCompatibility(compatibility);
    }
  }

  private List<GHTag> getProductReleaseTags(Product product) {
    try {
      return gitHubService.getRepositoryTags(product.getRepositoryName());
    } catch (IOException e) {
      log.error("Cannot get tag list of product ", e);
    }
    return List.of();
  }

  // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
  @Override
  public String getCompatibilityFromOldestTag(String oldestTag) {
    if (StringUtils.isBlank(oldestTag)) {
      return Strings.EMPTY;
    }
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
    Product product = productRepository.getProductById(id);
    return Optional.ofNullable(product).map(productItem -> {
      updateProductInstallationCount(id, productItem);
      return productItem;
    }).orElse(null);
  }

  @Override
  public Product fetchBestMatchProductDetail(String id, String version) {
    List<String> releasedVersions = productRepository.getReleasedVersionsById(id);
    String bestMatchVersion = VersionUtils.getBestMatchVersion(releasedVersions, version);
    String bestMatchTag = VersionUtils.convertVersionToTag(id, bestMatchVersion);
    Product product = StringUtils.isBlank(bestMatchTag) ? productRepository.getProductById(
        id) : productRepository.getProductByIdAndTag(id, bestMatchTag);
    return Optional.ofNullable(product).map(productItem -> {
      updateProductInstallationCount(id, productItem);
      return productItem;
    }).orElse(null);
  }

  public void updateProductInstallationCount(String id, Product productItem) {
    if (!BooleanUtils.isTrue(productItem.getSynchronizedInstallationCount())) {
      syncInstallationCountWithProduct(productItem);
      int persistedInitialCount = productRepository.updateInitialCount(id, productItem.getInstallationCount());
      productItem.setInstallationCount(persistedInitialCount);
    }
  }

  @Override
  public Product fetchProductDetailByIdAndVersion(String id, String version) {
    return productRepository.getProductByIdAndTag(id, VersionUtils.convertVersionToTag(id, version));
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
      productRepository.save(product);
      productEntries.add(product);
    }

    return productEntries;
  }

  public void removeFieldFromAllProductDocuments(String fieldName) {
    Update update = new Update().unset(fieldName);
    mongoTemplate.updateMulti(new Query(), update, Product.class);
  }

  public void transferComputedDataFromDB(Product product) {
    productRepository.findById(product.getId()).ifPresent(persistedData ->
        ProductFactory.transferComputedPersistedDataToProduct(persistedData, product)
    );
  }

}
