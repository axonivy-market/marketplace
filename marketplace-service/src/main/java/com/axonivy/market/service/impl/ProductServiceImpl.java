package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.MetaConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.ErrorCode;
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
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL;
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
  private static final String INITIAL_VERSION = "1.0";
  private final ProductRepository productRepo;
  private final ProductModuleContentRepository productModuleContentRepo;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final GHAxonIvyProductRepoService axonIvyProductRepoService;
  private final GitHubRepoMetaRepository gitHubRepoMetaRepo;
  private final GitHubService gitHubService;
  private final ProductCustomSortRepository productCustomSortRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final MetadataRepository metadataRepo;
  private final ProductJsonContentRepository productJsonContentRepo;
  private final ImageRepository imageRepo;
  private final ImageService imageService;
  private final MongoTemplate mongoTemplate;
  private final MetadataService metadataService;
  private final ProductContentService productContentService;
  private final ObjectMapper mapper = new ObjectMapper();
  private final SecureRandom random = new SecureRandom();
  private GHCommit lastGHCommit;
  private GitHubRepoMeta marketRepoMeta;
  @Value("${market.legacy.installation.counts.path}")
  private String legacyInstallationCountPath;
  @Value("${market.github.market.branch}")
  private String marketRepoBranch;

  public ProductServiceImpl(ProductRepository productRepo,
      ProductModuleContentRepository productModuleContentRepo,
      GHAxonIvyMarketRepoService axonIvyMarketRepoService, GHAxonIvyProductRepoService axonIvyProductRepoService,
      GitHubRepoMetaRepository gitHubRepoMetaRepo, GitHubService gitHubService,
      ProductCustomSortRepository productCustomSortRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo,
      ProductJsonContentRepository productJsonContentRepo, ImageRepository imageRepo, MetadataService metadataService,
      MetadataSyncRepository metadataSyncRepo, MetadataRepository metadataRepo, ImageService imageService,
      MongoTemplate mongoTemplate, ProductContentService productContentService) {
    this.productRepo = productRepo;
    this.productModuleContentRepo = productModuleContentRepo;
    this.axonIvyMarketRepoService = axonIvyMarketRepoService;
    this.axonIvyProductRepoService = axonIvyProductRepoService;
    this.gitHubRepoMetaRepo = gitHubRepoMetaRepo;
    this.gitHubService = gitHubService;
    this.productCustomSortRepo = productCustomSortRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.productJsonContentRepo = productJsonContentRepo;
    this.metadataSyncRepo = metadataSyncRepo;
    this.metadataRepo = metadataRepo;
    this.metadataService = metadataService;
    this.imageRepo = imageRepo;
    this.imageService = imageService;
    this.mongoTemplate = mongoTemplate;
    this.productContentService = productContentService;
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
    return productRepo.searchByCriteria(searchCriteria, searchPageable);
  }

  @Override
  public List<String> syncLatestDataFromMarketRepo(Boolean resetSync) {
    List<String> syncedProductIds = new ArrayList<>();
    var isAlreadyUpToDate = false;
    marketRepoMeta = gitHubRepoMetaRepo.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    if (BooleanUtils.isTrue(resetSync) && marketRepoMeta != null) {
      gitHubRepoMetaRepo.delete(marketRepoMeta);
      marketRepoMeta = null;
    } else {
      isAlreadyUpToDate = isLastGithubCommitCovered();
    }

    if (!isAlreadyUpToDate) {
      if (marketRepoMeta == null) {
        syncedProductIds = syncProductsFromGitHubRepo(resetSync);
        marketRepoMeta = new GitHubRepoMeta();
      } else {
        syncedProductIds = updateLatestChangeToProductsFromGithubRepo();
      }
      syncRepoMetaDataStatus();
    }
    updateLatestReleaseVersionContentsFromProductRepo();
    return syncedProductIds.stream().filter(StringUtils::isNotBlank).toList();
  }

  @Override
  public int updateInstallationCountForProduct(String key, String designerVersion) {
    Product product = productRepo.getProductWithModuleContent(key);
    if (Objects.isNull(product)) {
      return 0;
    }

    log.info("Increase installation count for product {} By Designer Version {}", key, designerVersion);
    if (StringUtils.isNotBlank(designerVersion)) {
      productRepo.increaseInstallationCountForProductByDesignerVersion(key, designerVersion);
    }

    log.info("updating installation count for product {}", key);
    if (BooleanUtils.isTrue(product.getSynchronizedInstallationCount())) {
      return productRepo.increaseInstallationCount(key);
    }
    syncInstallationCountWithProduct(product);
    return productRepo.updateInitialCount(key, product.getInstallationCount() + 1);
  }

  public void syncInstallationCountWithProduct(Product product) {
    log.info("synchronizing installation count for product {}", product.getId());
    try {
      String installationCounts = Files.readString(Paths.get(legacyInstallationCountPath));
      Map<String, Integer> mapping = mapper.readValue(installationCounts,
          new TypeReference<HashMap<String, Integer>>() {
          });
      List<String> keyList = mapping.keySet().stream().toList();
      int currentInstallationCount = keyList.contains(product.getId())
          ? mapping.get(product.getId()) : random.nextInt(20, 50);
      product.setInstallationCount(currentInstallationCount);
      product.setSynchronizedInstallationCount(true);
      log.info("synchronized installation count for product {} successfully", product.getId());
    } catch (IOException ex) {
      log.error("Could not read the marketplace-installation file to synchronize", ex);
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
    gitHubRepoMetaRepo.save(marketRepoMeta);
    marketRepoMeta = null;
  }

  private List<String> updateLatestChangeToProductsFromGithubRepo() {
    Set<String> syncedProductIds = new HashSet<>();
    var fromSHA1 = marketRepoMeta.getLastSHA1();
    var toSHA1 = ofNullable(lastGHCommit).map(GHCommit::getSHA1).orElse(EMPTY);
    log.warn("**ProductService: synchronize products from SHA1 {} to SHA1 {}", fromSHA1, toSHA1);
    List<GitHubFile> gitHubFileChanges = axonIvyMarketRepoService.fetchMarketItemsBySHA1Range(fromSHA1, toSHA1);
    Map<String, List<GitHubFile>> groupGitHubFiles = new HashMap<>();
    for (var file : gitHubFileChanges) {
      String filePath = file.getFileName();
      var parentPath = filePath.substring(0, filePath.lastIndexOf(CommonConstants.SLASH) + 1);
      var files = groupGitHubFiles.getOrDefault(parentPath, new ArrayList<>());
      files.add(file);
      files.sort((file1, file2) -> GitHubUtils.sortMetaJsonFirst(file1.getFileName(), file2.getFileName()));
      groupGitHubFiles.putIfAbsent(parentPath, files);
    }

    groupGitHubFiles.forEach((key, value) -> {
      for (var file : value) {
        var productId = EMPTY;
        if (file.getStatus() == MODIFIED || file.getStatus() == ADDED) {
          productId = modifyProductMetaOrLogo(file, key);
        } else {
          productId = removeProductAndImage(file);
        }
        syncedProductIds.add(productId);
      }
    });
    return syncedProductIds.stream().toList();
  }

  private String removeProductAndImage(GitHubFile file) {
    String productId = EMPTY;
    if (FileType.META == file.getType()) {
      String[] splitMetaJsonPath = file.getFileName().split(SLASH);
      String extractMarketDirectory = file.getFileName().replace(splitMetaJsonPath[splitMetaJsonPath.length - 1],
          EMPTY);
      List<Product> productList = productRepo.findByMarketDirectory(extractMarketDirectory);
      if (ObjectUtils.isNotEmpty(productList)) {
        productId = productList.get(0).getId();
        productRepo.deleteById(productId);
        imageRepo.deleteAllByProductId(productId);
      }
    } else {
      List<Image> images = imageRepo.findByImageUrlEndsWithIgnoreCase(file.getFileName());
      if (ObjectUtils.isNotEmpty(images)) {
        Image currentImage = images.get(0);
        productId = currentImage.getProductId();
        productRepo.deleteById(productId);
        imageRepo.deleteAllByProductId(productId);
      }
    }
    return productId;
  }

  private String modifyProductMetaOrLogo(GitHubFile file, String parentPath) {
    try {
      GHContent fileContent = gitHubService.getGHContent(axonIvyMarketRepoService.getRepository(), file.getFileName(),
          marketRepoBranch);
      return updateProductByMetaJsonAndLogo(fileContent, file, parentPath);
    } catch (IOException e) {
      log.error("Get GHContent failed: ", e);
    }
    return EMPTY;
  }

  private String updateProductByMetaJsonAndLogo(GHContent fileContent, GitHubFile file, String parentPath) {
    String productId;
    if (FileType.META == file.getType()) {
      Product product = new Product();
      ProductFactory.mappingByGHContent(product, fileContent);
      mappingVendorImageFromGHContent(product, fileContent);
      transferComputedDataFromDB(product);
      productId = productRepo.save(product).getId();
    } else {
      productId = modifyProductLogo(parentPath, fileContent);
    }
    return productId;
  }

  private String modifyProductLogo(String parentPath, GHContent fileContent) {
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setKeyword(parentPath);
    searchCriteria.setFields(List.of(MARKET_DIRECTORY));
    Product result = productRepo.findByCriteria(searchCriteria);
    if (result != null) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(result.getId(), fileContent, true)).ifPresent(image -> {
        if (StringUtils.isNotBlank(result.getLogoId())) {
          imageRepo.deleteById(result.getLogoId());
        }
        result.setLogoId(image.getId());
        productRepo.save(result);
      });
      return result.getId();
    }
    log.info("There is no product to update the logo with path {}", parentPath);
    return EMPTY;
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
    List<ProductCustomSort> customSorts = productCustomSortRepo.findAll();

    if (!customSorts.isEmpty()) {
      SortOption sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      return createOrder(sortOptionExtension, language);
    }
    return createOrder(SortOption.POPULARITY, language);
  }

  private boolean isLastGithubCommitCovered() {
    boolean isLastCommitCovered = false;
    long lastCommitTime = 0L;
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

  private void updateLatestReleaseVersionContentsFromProductRepo() {
    List<Product> products = productRepo.findAll();
    if (ObjectUtils.isEmpty(products)) {
      return;
    }

    for (Product product : products) {
      updateProductFromReleasedVersions(product);
      productRepo.save(product);
    }
  }

  private List<String> syncProductsFromGitHubRepo(Boolean resetSync) {
    log.warn("**ProductService: synchronize products from scratch based on the Market repo");
    List<String> syncedProductIds = new ArrayList<>();
    var gitHubContentMap = axonIvyMarketRepoService.fetchAllMarketItems();
    for (Map.Entry<String, List<GHContent>> ghContentEntity : gitHubContentMap.entrySet()) {
      var product = new Product();
      //update the meta.json first
      ghContentEntity.getValue().sort((f1, f2) -> GitHubUtils.sortMetaJsonFirst(f1.getName(), f2.getName()));

      for (var content : ghContentEntity.getValue()) {
        ProductFactory.mappingByGHContent(product, content);
        mappingVendorImageFromGHContent(product, content);
        mappingLogoFromGHContent(product, content);
      }
      if (productRepo.findById(product.getId()).isPresent() && BooleanUtils.isNotTrue(resetSync)) {
        continue;
      }

      updateProductContentForNonStandardProduct(ghContentEntity.getValue(), product);
      updateProductFromReleasedVersions(product);
      transferComputedDataFromDB(product);
      syncedProductIds.add(productRepo.save(product).getId());
    }
    return syncedProductIds;
  }

  private void mappingLogoFromGHContent(Product product, GHContent ghContent) {
    if (ghContent != null && StringUtils.endsWith(ghContent.getName(), LOGO_FILE)) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(product.getId(), ghContent, true))
          .ifPresent(image -> product.setLogoId(image.getId()));
    }
  }

  private void mappingVendorImageFromGHContent(Product product, GHContent ghContent) {
    if (StringUtils.endsWith(ghContent.getName(), MetaConstants.META_FILE)) {
      if (StringUtils.isNotBlank(product.getVendorImagePath())) {
        product.setVendorImage(mapVendorImage(product.getId(), ghContent, product.getVendorImagePath()));
      }
      if (StringUtils.isNotBlank(product.getVendorImageDarkModePath())) {
        product.setVendorImageDarkMode(mapVendorImage(product.getId(), ghContent, product.getVendorImageDarkModePath()));
      }
    }
  }

  private String mapVendorImage(String productId, GHContent ghContent, String imageName) {
    if (StringUtils.isNotBlank(imageName)) {
      String imagePath = StringUtils.replace(ghContent.getPath(), MetaConstants.META_FILE, imageName);
      try {
        GHContent imageContent = gitHubService.getGHContent(ghContent.getOwner(), imagePath, marketRepoBranch);
        return Optional.ofNullable(imageService.mappingImageFromGHContent(productId, imageContent, false))
            .map(Image::getId).orElse(EMPTY);
      } catch (IOException e) {
        log.error("Get Vendor Image failed: ", e);
      }
    }
    return EMPTY;
  }

  private void updateProductFromReleasedVersions(Product product) {
    if (ObjectUtils.isEmpty(product.getArtifacts())) {
      return;
    }

    product.getArtifacts().stream()
        .filter(artifact -> artifact.getArtifactId().contains(MavenConstants.PRODUCT_ARTIFACT_POSTFIX))
        .findAny()
        .ifPresent(mavenArtifact -> {
          String metadataUrl = MavenUtils.buildMetadataUrlFromArtifactInfo(mavenArtifact.getRepoUrl(),
              mavenArtifact.getGroupId(), mavenArtifact.getArtifactId());
          String metadataContent = MavenUtils.getMetadataContentFromUrl(metadataUrl);
          if (StringUtils.isNotBlank(metadataContent)) {
            updateContentsFromMavenXML(product, metadataContent, mavenArtifact);
          }
        });
  }

  private void updateContentsFromMavenXML(Product product, String metadataContent, Artifact mavenArtifact) {
    try {
      Document document = MetadataReaderUtils.getDocumentFromXMLContent(metadataContent);

      String latestVersion = MetadataReaderUtils.getElementValue(document, MavenConstants.LATEST_VERSION_TAG);
      if (StringUtils.equals(latestVersion, product.getNewestReleaseVersion())) {
        return;
      }
      product.setNewestPublishedDate(getNewestPublishedDate(document));
      product.setNewestReleaseVersion(latestVersion);

      NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);
      List<String> mavenVersions = new ArrayList<>();
      for (int i = 0; i < versionNodes.getLength(); i++) {
        mavenVersions.add(versionNodes.item(i).getTextContent());
      }

      updateProductCompatibility(product, mavenVersions);

      List<String> currentVersions = product.getReleasedVersions();
      if (CollectionUtils.isEmpty(currentVersions)) {
        product.setReleasedVersions(new ArrayList<>());
        currentVersions = productModuleContentRepo.findVersionsByProductId(product.getId());
      }
      mavenVersions = mavenVersions.stream().filter(filterNonPersistVersion(currentVersions)).toList();

      List<ProductModuleContent> productModuleContents = new ArrayList<>();
      for (String version : mavenVersions) {
        product.getReleasedVersions().add(version);
        handleProductArtifact(version, product, productModuleContents, mavenArtifact);
      }

      if (ObjectUtils.isNotEmpty(productModuleContents)) {
        productModuleContentRepo.saveAll(productModuleContents);
      }
    } catch (Exception e) {
      log.error("Metadata Reader: can not read the metadata of {} with error", metadataContent, e);
    }
  }

  private Date getNewestPublishedDate(Document document) {
    DateTimeFormatter lastUpdatedFormatter = DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT);
    LocalDateTime newestPublishedDate =
        LocalDateTime.parse(Objects.requireNonNull(MetadataReaderUtils.getElementValue(document,
            MavenConstants.LAST_UPDATED_TAG)), lastUpdatedFormatter);
    return Date.from(newestPublishedDate.atZone(ZoneOffset.UTC).toInstant());
  }

  private void updateProductCompatibility(Product product, List<String> mavenVersions) {
    if (StringUtils.isBlank(product.getCompatibility())) {
      String oldestVersion = VersionUtils.getOldestVersions(mavenVersions);
      if (oldestVersion != null) {
        String compatibility = getCompatibilityFromOldestVersion(oldestVersion);
        product.setCompatibility(compatibility);
      }
    }
  }

  private static Predicate<? super String> filterNonPersistVersion(List<String> currentVersions) {
    return version -> !currentVersions.contains(version);
  }

  public void handleProductArtifact(String version, Product product,
      List<ProductModuleContent> productModuleContents, Artifact mavenArtifact) {
    String snapshotVersionValue = Strings.EMPTY;
    if (version.contains(MavenConstants.SNAPSHOT_VERSION)) {
      snapshotVersionValue = MetadataReaderUtils.getSnapshotVersionValue(version, mavenArtifact);
    }
    String repoUrl = StringUtils.defaultIfBlank(mavenArtifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);
    String url = MavenUtils.buildDownloadUrl(mavenArtifact.getArtifactId(), version, mavenArtifact.getType(),
        repoUrl, mavenArtifact.getGroupId(), StringUtils.defaultIfBlank(snapshotVersionValue, version));

    if (StringUtils.isBlank(url)) {
      return;
    }

    try {
      addProductContent(product, version, url, productModuleContents, mavenArtifact);
    } catch (Exception e) {
      log.error("Cannot download and unzip file {}", e.getMessage());
    }
  }

  public void addProductContent(Product product, String version, String url,
      List<ProductModuleContent> productModuleContents, Artifact artifact) {
    ProductModuleContent productModuleContent = productContentService.getReadmeAndProductContentsFromVersion(product,
        version, url, artifact);
    if (Objects.nonNull(productModuleContent)) {
      productModuleContents.add(productModuleContent);
    }
  }

  // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
  @Override
  public String getCompatibilityFromOldestVersion(String oldestVersion) {
    if (StringUtils.isBlank(oldestVersion)) {
      return Strings.EMPTY;
    }
    if (!oldestVersion.contains(CommonConstants.DOT_SEPARATOR)) {
      return oldestVersion + ".0+";
    }
    int firstDot = oldestVersion.indexOf(CommonConstants.DOT_SEPARATOR);
    int secondDot = oldestVersion.indexOf(CommonConstants.DOT_SEPARATOR, firstDot + 1);
    if (secondDot == -1) {
      return oldestVersion.concat(CommonConstants.PLUS);
    }
    return oldestVersion.substring(0, secondDot).concat(CommonConstants.PLUS);
  }

  @Override
  public Product fetchProductDetail(String id, Boolean isShowDevVersion) {
    Product product = getProductByIdWithNewestReleaseVersion(id, isShowDevVersion);
    return Optional.ofNullable(product).map(productItem -> {
      updateProductInstallationCount(id, productItem);
      return productItem;
    }).orElse(null);
  }

  @Override
  public Product fetchBestMatchProductDetail(String id, String version) {
    List<String> installableVersions = VersionUtils.getInstallableVersionsFromMetadataList(
        metadataRepo.findByProductId(id));
    String bestMatchVersion = VersionUtils.getBestMatchVersion(installableVersions, version);
       // Cover exception case of employee onboarding without any product.json file
    Product product = StringUtils.isBlank(bestMatchVersion) ? getProductByIdWithNewestReleaseVersion(id,
        false) : productRepo.getProductByIdAndVersion(id, bestMatchVersion);
    return Optional.ofNullable(product).map(productItem -> {
      updateProductInstallationCount(id, productItem);
      productItem.setBestMatchVersion(bestMatchVersion);
      return productItem;
    }).orElse(null);
  }

  public Product getProductByIdWithNewestReleaseVersion(String id, Boolean isShowDevVersion) {
    List<String> versions;
    String version = StringUtils.EMPTY;

    var mavenArtifactVersion = mavenArtifactVersionRepo.findById(id);
    if (mavenArtifactVersion.isPresent()) {
      versions = MavenUtils.getAllExistingVersions(mavenArtifactVersion.get(), BooleanUtils.isTrue(isShowDevVersion),
          StringUtils.EMPTY);
      version = CollectionUtils.firstElement(versions);
    }

    // Cover exception case of employee onboarding without any product.json file
    if (StringUtils.isBlank(version)) {
      versions = VersionUtils.getVersionsToDisplay(productRepo.getReleasedVersionsById(id), isShowDevVersion,
          StringUtils.EMPTY);
      version = CollectionUtils.firstElement(versions);
    }

    Product product = productRepo.getProductByIdAndVersion(id, version);
    productJsonContentRepo.findByProductIdAndVersion(id, version).stream().map(
        ProductJsonContent::getContent).findFirst().ifPresent(
        jsonContent -> product.setMavenDropins(MavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent)));
    return product;
  }

  public void updateProductInstallationCount(String id, Product productItem) {
    if (!BooleanUtils.isTrue(productItem.getSynchronizedInstallationCount())) {
      syncInstallationCountWithProduct(productItem);
      int persistedInitialCount = productRepo.updateInitialCount(id, productItem.getInstallationCount());
      productItem.setInstallationCount(persistedInitialCount);
    }
  }

  @Override
  public Product fetchProductDetailByIdAndVersion(String id, String version) {
    return productRepo.getProductByIdAndVersion(id, version);
  }

  @Override
  public void addCustomSortProduct(ProductCustomSortRequest customSort) throws InvalidParamException {
    SortOption.of(customSort.getRuleForRemainder());

    ProductCustomSort productCustomSort = new ProductCustomSort(customSort.getRuleForRemainder());
    productCustomSortRepo.deleteAll();
    removeFieldFromAllProductDocuments(ProductJsonConstants.CUSTOM_ORDER);
    productCustomSortRepo.save(productCustomSort);
    productRepo.saveAll(refineOrderedListOfProductsInCustomSort(customSort.getOrderedListOfProducts()));
  }

  public List<Product> refineOrderedListOfProductsInCustomSort(List<String> orderedListOfProducts)
      throws InvalidParamException {
    List<Product> productEntries = new ArrayList<>();

    int descendingOrder = orderedListOfProducts.size();
    for (String productId : orderedListOfProducts) {
      Optional<Product> productOptional = productRepo.findById(productId);

      if (productOptional.isEmpty()) {
        throw new InvalidParamException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
      }
      Product product = productOptional.get();
      product.setCustomOrder(descendingOrder--);
      productRepo.save(product);
      productEntries.add(product);
    }

    return productEntries;
  }

  public void removeFieldFromAllProductDocuments(String fieldName) {
    Update update = new Update().unset(fieldName);
    mongoTemplate.updateMulti(new Query(), update, Product.class);
  }

  public void transferComputedDataFromDB(Product product) {
    productRepo.findById(product.getId()).ifPresent(persistedData ->
        ProductFactory.transferComputedPersistedDataToProduct(persistedData, product)
    );
  }

  @Override
  public boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
    try {
      log.info("Sync product {} is starting ...", productId);
      log.info("Clean up product {}", productId);
      Product product = renewProductById(productId, marketItemPath, overrideMarketItemPath);
      log.info("Get data of product {} from the git hub", productId);
      var gitHubContents = axonIvyMarketRepoService.getMarketItemByPath(product.getMarketDirectory());
      if (!CollectionUtils.isEmpty(gitHubContents)) {
        log.info("Update data of product {} from meta.json and logo files", productId);
        mappingMetaDataAndLogoFromGHContent(gitHubContents, product);
        updateProductContentForNonStandardProduct(gitHubContents, product);
        updateProductFromReleasedVersions(product);
        productRepo.save(product);
        metadataService.syncProductMetadata(product);
        log.info("Sync product {} is finished!", productId);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getStackTrace());
    }
    return false;
  }

  @Override
  public void clearAllProductVersion() {
    metadataRepo.deleteAll();
    metadataSyncRepo.deleteAll();
    mavenArtifactVersionRepo.deleteAll();
  }

  private Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
    Product product = new Product();
    productRepo.findById(productId).ifPresent(foundProduct -> {
          ProductFactory.transferComputedPersistedDataToProduct(foundProduct, product);
          imageRepo.deleteAllByProductId(foundProduct.getId());
          metadataRepo.deleteAllByProductId(foundProduct.getId());
          metadataSyncRepo.deleteAllByProductId(foundProduct.getId());
          mavenArtifactVersionRepo.deleteAllById(List.of(foundProduct.getId()));
          productModuleContentRepo.deleteAllByProductId(foundProduct.getId());
          productJsonContentRepo.deleteAllByProductId(foundProduct.getId());
          productRepo.delete(foundProduct);
        }
    );

    if (StringUtils.isNotBlank(marketItemPath) && Boolean.TRUE.equals(overrideMarketItemPath)) {
      product.setMarketDirectory(marketItemPath);
    }
    product.setNewestReleaseVersion(EMPTY);

    return product;
  }

  private void mappingMetaDataAndLogoFromGHContent(List<GHContent> gitHubContent, Product product) {
    var gitHubContents = new ArrayList<>(gitHubContent);
    gitHubContents.sort((f1, f2) -> GitHubUtils.sortMetaJsonFirst(f1.getName(), f2.getName()));
    for (var content : gitHubContent) {
      ProductFactory.mappingByGHContent(product, content);
      mappingVendorImageFromGHContent(product, content);
      mappingLogoFromGHContent(product, content);
    }
  }

  private void updateProductContentForNonStandardProduct(List<GHContent> ghContentEntity,
      Product product) {
    if (StringUtils.isBlank(product.getRepositoryName())) {
      ProductModuleContent initialContent = new ProductModuleContent();
      initialContent.setVersion(INITIAL_VERSION);
      initialContent.setProductId(product.getId());
      ProductFactory.mappingIdForProductModuleContent(initialContent);
      product.setReleasedVersions(List.of(INITIAL_VERSION));
      product.setNewestReleaseVersion(INITIAL_VERSION);
      axonIvyProductRepoService.extractReadMeFileFromContents(product, ghContentEntity, initialContent);
      productModuleContentRepo.save(initialContent);
    }
  }
}
