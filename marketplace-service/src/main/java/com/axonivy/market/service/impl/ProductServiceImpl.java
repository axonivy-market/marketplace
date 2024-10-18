package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.MetaConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
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
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.ProductContentUtils;
import com.axonivy.market.util.VersionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
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
import java.util.stream.Stream;

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
  private static final String INITIAL_VERSION = "1.0";
  private final ProductRepository productRepository;
  private final ProductModuleContentRepository productModuleContentRepository;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final GHAxonIvyProductRepoService axonIvyProductRepoService;
  private final GitHubRepoMetaRepository gitHubRepoMetaRepository;
  private final GitHubService gitHubService;
  private final ProductCustomSortRepository productCustomSortRepository;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataSyncRepository metadataSyncRepository;
  private final MetadataRepository metadataRepository;
  private final ProductJsonContentRepository productJsonContentRepository;
  private final ImageRepository imageRepository;
  private final ImageService imageService;
  private final MongoTemplate mongoTemplate;
  private final MetadataService metadataService;
  private final FileDownloadService fileDownloadService;
  private final ProductJsonContentService productJsonContentService;
  private final ObjectMapper mapper = new ObjectMapper();
  private final SecureRandom random = new SecureRandom();
  private GHCommit lastGHCommit;
  private GitHubRepoMeta marketRepoMeta;
  @Value("${market.legacy.installation.counts.path}")
  private String legacyInstallationCountPath;
  @Value("${market.github.market.branch}")
  private String marketRepoBranch;
  private static final int EQUAL = 0;

  public ProductServiceImpl(ProductRepository productRepository,
      ProductModuleContentRepository productModuleContentRepository,
      GHAxonIvyMarketRepoService axonIvyMarketRepoService, GHAxonIvyProductRepoService axonIvyProductRepoService,
      GitHubRepoMetaRepository gitHubRepoMetaRepository, GitHubService gitHubService,
      ProductCustomSortRepository productCustomSortRepository, MavenArtifactVersionRepository mavenArtifactVersionRepo,
      ImageRepository imageRepository, MetadataService metadataService, MetadataSyncRepository metadataSyncRepository,
      MetadataRepository metadataRepository, ImageService imageService, MongoTemplate mongoTemplate,
      ProductJsonContentRepository productJsonContentRepository, FileDownloadService fileDownloadService,
      ProductJsonContentService productJsonContentService) {
    this.productRepository = productRepository;
    this.productModuleContentRepository = productModuleContentRepository;
    this.axonIvyMarketRepoService = axonIvyMarketRepoService;
    this.axonIvyProductRepoService = axonIvyProductRepoService;
    this.gitHubRepoMetaRepository = gitHubRepoMetaRepository;
    this.gitHubService = gitHubService;
    this.productCustomSortRepository = productCustomSortRepository;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.metadataSyncRepository = metadataSyncRepository;
    this.metadataRepository = metadataRepository;
    this.metadataService = metadataService;
    this.imageRepository = imageRepository;
    this.imageService = imageService;
    this.mongoTemplate = mongoTemplate;
    this.productJsonContentRepository = productJsonContentRepository;
    this.fileDownloadService = fileDownloadService;
    this.productJsonContentService = productJsonContentService;
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
  public List<String> syncLatestDataFromMarketRepo() {
    List<String> syncedProductIds = new ArrayList<>();
    var isAlreadyUpToDate = isLastGithubCommitCovered();
    if (!isAlreadyUpToDate) {
      if (marketRepoMeta == null) {
        syncedProductIds = syncProductsFromGitHubRepo();
        marketRepoMeta = new GitHubRepoMeta();
      } else {
        syncedProductIds = updateLatestChangeToProductsFromGithubRepo();
      }
      syncRepoMetaDataStatus();
    }
    updateLatestReleaseTagContentsFromProductRepo();
    return syncedProductIds.stream().filter(StringUtils::isNotBlank).toList();
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
    gitHubRepoMetaRepository.save(marketRepoMeta);
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
      List<Product> productList = productRepository.findByMarketDirectory(extractMarketDirectory);
      if (ObjectUtils.isNotEmpty(productList)) {
        productId = productList.get(0).getId();
        productRepository.deleteById(productId);
        imageRepository.deleteAllByProductId(productId);
      }
    } else {
      List<Image> images = imageRepository.findByImageUrlEndsWithIgnoreCase(file.getFileName());
      if (ObjectUtils.isNotEmpty(images)) {
        Image currentImage = images.get(0);
        productId = currentImage.getProductId();
        productRepository.deleteById(productId);
        imageRepository.deleteAllByProductId(productId);
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
      productId = productRepository.save(product).getId();
    } else {
      productId = modifyProductLogo(parentPath, fileContent);
    }
    return productId;
  }

  private String modifyProductLogo(String parentPath, GHContent fileContent) {
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setKeyword(parentPath);
    searchCriteria.setFields(List.of(MARKET_DIRECTORY));
    Product result = productRepository.findByCriteria(searchCriteria);
    if (result != null) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(result.getId(), fileContent, true)).ifPresent(image -> {
        if (StringUtils.isNotBlank(result.getLogoId())) {
          imageRepository.deleteById(result.getLogoId());
        }
        result.setLogoId(image.getId());
        productRepository.save(result);
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
        updateProductFromReleaseTags(product);
        productRepository.save(product);
      }
    }
  }

  private List<String> syncProductsFromGitHubRepo() {
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
      if (productRepository.findById(product.getId()).isPresent()) {
        continue;
      }
      updateRelatedThingsOfProductFromGHContent(ghContentEntity.getValue(), product);
      transferComputedDataFromDB(product);
      syncedProductIds.add(productRepository.save(product).getId());
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
        product.setVendorImageDarkMode(
            mapVendorImage(product.getId(), ghContent, product.getVendorImageDarkModePath()));
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

  private void updateProductFromReleaseTags(Product product) {
    Artifact mavenArtifact = product.getArtifacts().stream()
        .filter(artifact -> artifact.getArtifactId().contains(MavenConstants.PRODUCT_ARTIFACT_POSTFIX))
        .findAny().orElse(null);
    if (mavenArtifact == null) {
      return;
    }
    String metadataUrl = MavenUtils.buildMetadataUrlFromArtifactInfo(mavenArtifact.getRepoUrl(),
        mavenArtifact.getGroupId(), mavenArtifact.getArtifactId());
    String metadataContent = MavenUtils.getMetadataContentFromUrl(metadataUrl);

    updateContentsFromMavenXML(product, metadataContent, mavenArtifact);
  }

  private void updateContentsFromMavenXML(Product product, String metadataContent, Artifact mavenArtifact) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(metadataContent)));
      document.getDocumentElement().normalize();
      NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);

      String highestVersion = versionNodes.item(0).getTextContent();
      for (int i = 0; i < versionNodes.getLength(); i++) {
        if (MavenVersionComparator.compare(versionNodes.item(i).getTextContent(), highestVersion) > EQUAL) {
          highestVersion = versionNodes.item(i).getTextContent();
        }

        if (Objects.isNull(product.getReleasedVersions())) {
          product.setReleasedVersions(new ArrayList<>());
        }
        product.getReleasedVersions().add(versionNodes.item(i).getTextContent());
      }

      product.setNewestReleaseVersion(highestVersion);

      List<ProductModuleContent> productModuleContents = new ArrayList<>();
      for (String version : product.getReleasedVersions()) {
        handleProductArtifact(version, product, productModuleContents, mavenArtifact);
      }

      if (ObjectUtils.isNotEmpty(productModuleContents)) {
        productModuleContentRepository.saveAll(productModuleContents);
      }

    } catch (Exception e) {
      log.error("Metadata Reader: can not read the metadata of {} with error", metadataContent, e);
    }

  }

  //TODO: Set other fields

//    private void updateProductFromReleaseTags1(Product product, GHRepository productRepo) {
//    List<ProductModuleContent> productModuleContents = new ArrayList<>();
//    List<GHTag> ghTags = getProductReleaseTags(product);
//    GHTag lastTag = MavenVersionComparator.findHighestTag(ghTags);
//    if (lastTag == null || lastTag.getName().equals(product.getNewestReleaseVersion())) {
//      return;
//    }
//    product.setNewestPublishedDate(getPublishedDateFromLatestTag(lastTag));
//    product.setNewestReleaseVersion(lastTag.getName());
//    List<String> currentTags = VersionUtils.getReleaseTagsFromProduct(product);
//    if (CollectionUtils.isEmpty(currentTags)) {
//      currentTags = productModuleContentRepository.findTagsByProductId(product.getId());
//    }
//    ghTags = ghTags.stream().filter(filterNonPersistGhTagName(currentTags)).toList();
//
//    for (GHTag ghTag : ghTags) {
//      ProductModuleContent productModuleContent =
//          axonIvyProductRepoService.getReadmeAndProductContentsFromTag(product, productRepo, ghTag.getName());
//      if (productModuleContent != null) {
//        productModuleContents.add(productModuleContent);
//      }
//      String versionFromTag = VersionUtils.convertTagToVersion(ghTag.getName());
//      if (Objects.isNull(product.getReleasedVersions())) {
//        product.setReleasedVersions(new ArrayList<>());
//      }
//      product.getReleasedVersions().add(versionFromTag);
//    }
//    if (!CollectionUtils.isEmpty(productModuleContents)) {
//      productModuleContentRepository.saveAll(productModuleContents);
//    }
//  }

  public void handleProductArtifact(String version, Product product,
      List<ProductModuleContent> productModuleContents, Artifact mavenArtifact) {
    // TODO: Snapshot version
//    if (version.contains(MavenConstants.SNAPSHOT_VERSION)) {
//      Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(productArtifact,
//      nonMatchSnapshotVersion);
//      MetadataReaderUtils.updateMetadataFromMavenXML(
//          MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl()), snapShotMetadata, true);
//
//      String url = buildProductFolderDownloadUrl(snapShotMetadata, nonMatchSnapshotVersion);

    String url = buildProductFolderDownloadUrl(version, mavenArtifact);

    if (StringUtils.isBlank(url)) {
      return;
    }

    try {
      addProductContent(product, version, url, productModuleContents, mavenArtifact);
    } catch (Exception e) {
      log.error("Cannot download and unzip file {}", e.getMessage());
    }
  }

  public String buildProductFolderDownloadUrl(String version, Artifact mavenArtifact) {
    return MavenUtils.buildDownloadUrl(
        mavenArtifact.getArtifactId(), version,
        MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE,
        mavenArtifact.getRepoUrl(), mavenArtifact.getGroupId(), version);
  }

  public void addProductContent(Product product, String version, String url,
      List<ProductModuleContent> productModuleContents, Artifact artifact) {
    ProductModuleContent productModuleContent = getReadmeAndProductContentsFromTag(product, version, url, artifact);
    if (Objects.nonNull(productModuleContent)) {
      productModuleContents.add(productModuleContent);
    }
  }

  private ProductModuleContent getReadmeAndProductContentsFromTag(Product product, String version, String url,
      Artifact artifact) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(product.getId(),
        version);
    String unzippedFolderPath = Strings.EMPTY;
    try {
      unzippedFolderPath = fileDownloadService.downloadAndUnzipProductContentFile(url, artifact);
      updateDependencyContentsFromProductJson(productModuleContent, product, unzippedFolderPath);
      extractReadMeFileFromContents(product.getId(), unzippedFolderPath, productModuleContent);
    } catch (Exception e) {
      log.error("Cannot get product.json content in {}", e.getMessage());
      return null;
    } finally {
      if (StringUtils.isNotBlank(unzippedFolderPath)) {
        fileDownloadService.deleteDirectory(Path.of(unzippedFolderPath));
      }
    }
    return productModuleContent;
  }

  private void updateDependencyContentsFromProductJson(ProductModuleContent productModuleContent,
      Product product, String unzippedFolderPath) throws IOException {
    List<Artifact> artifacts = MavenUtils.convertProductJsonToMavenProductInfo(
        Paths.get(unzippedFolderPath));
    ProductContentUtils.updateProductModule(productModuleContent, artifacts);
//    String currentVersion = productModuleContent.getMavenVersions().stream().findAny().orElse(null);
    Path productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
    String content = extractProductJsonContent(productJsonPath);
    productJsonContentService.updateProductJsonContent(content, null, productModuleContent.getTag(),
        ProductJsonConstants.VERSION_VALUE, product);
  }

  private void extractReadMeFileFromContents(String productId, String unzippedFolderPath,
      ProductModuleContent productModuleContent) {
    try {
      List<Path> readmeFiles;
      Map<String, Map<String, String>> moduleContents = new HashMap<>();
      try (Stream<Path> readmePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
        readmeFiles = readmePathStream.filter(Files::isRegularFile).filter(
            path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME)).toList();
      }
      if (ObjectUtils.isNotEmpty(readmeFiles)) {
        for (Path readmeFile : readmeFiles) {
          String readmeContents = Files.readString(readmeFile);
          if (ProductContentUtils.hasImageDirectives(readmeContents)) {
            readmeContents = updateImagesWithDownloadUrl(productId, unzippedFolderPath, readmeContents);
          }
          ProductContentUtils.getExtractedPartsOfReadme(moduleContents, readmeContents,
              readmeFile.getFileName().toString());
        }
        ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
      }
    } catch (Exception e) {
      log.error("Cannot get README file's content from folder {}: {}", unzippedFolderPath, e.getMessage());
    }
  }

  private String updateImagesWithDownloadUrl(String productId, String unzippedFolderPath,
      String readmeContents) throws IOException {
    List<Path> allImagePaths;
    Map<String, String> imageUrls = new HashMap<>();
    try (Stream<Path> imagePathStream = Files.walk(Paths.get(unzippedFolderPath))) {
      allImagePaths = imagePathStream.filter(Files::isRegularFile).filter(
          path -> path.getFileName().toString().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)).toList();
    }
    allImagePaths.forEach(
        imagePath -> Optional.of(imageService.mappingImageFromDownloadedFolder(productId, imagePath)).ifPresent(
            image -> imageUrls.put(imagePath.getFileName().toString(),
                CommonConstants.IMAGE_ID_PREFIX.concat(image.getId()))));

    return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
  }

  private String extractProductJsonContent(Path filePath) {
    try {
      InputStream contentStream = MavenUtils.extractedContentStream(filePath);
      return IOUtils.toString(Objects.requireNonNull(contentStream), StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error("Cannot extract product.json file {}", e.getMessage());
      return null;
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
  public Product fetchProductDetail(String id, Boolean isShowDevVersion) {
    Product product = productRepository.getProductByIdWithNewestReleaseVersion(id, isShowDevVersion);
    return Optional.ofNullable(product).map(productItem -> {
      updateProductInstallationCount(id, productItem);
      return productItem;
    }).orElse(null);
  }

  @Override
  public Product fetchBestMatchProductDetail(String id, String version) {
    MavenArtifactVersion existingMavenArtifactVersion = mavenArtifactVersionRepo.findById(id).orElse(
        MavenArtifactVersion.builder().productId(id).build());
    List<String> versions = MavenUtils.getAllExistingVersions(existingMavenArtifactVersion, true,
        null);
    String bestMatchVersion = VersionUtils.getBestMatchVersion(versions, version);
    String bestMatchTag = VersionUtils.convertVersionToTag(id, bestMatchVersion);
    Product product = StringUtils.isBlank(bestMatchTag) ? productRepository.getProductByIdWithNewestReleaseVersion(
        id, false) : productRepository.getProductByIdWithTagOrVersion(id, bestMatchTag);
    return Optional.ofNullable(product).map(productItem -> {
      updateProductInstallationCount(id, productItem);
      productItem.setBestMatchVersion(bestMatchVersion);
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
    return productRepository.getProductByIdWithTagOrVersion(id, version);
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
        updateRelatedThingsOfProductFromGHContent(gitHubContents, product);
        productRepository.save(product);
        metadataService.syncProductMetadata(product);
        log.info("Sync product {} is finished!", productId);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getStackTrace());
    }
    return false;
  }

  private Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
    Product product = new Product();
    productRepository.findById(productId).ifPresent(foundProduct -> {
          ProductFactory.transferComputedPersistedDataToProduct(foundProduct, product);
          imageRepository.deleteAllByProductId(foundProduct.getId());
          metadataRepository.deleteAllByProductId(foundProduct.getId());
          metadataSyncRepository.deleteAllByProductId(foundProduct.getId());
          mavenArtifactVersionRepo.deleteAllById(List.of(foundProduct.getId()));
          productModuleContentRepository.deleteAllByProductId(foundProduct.getId());
          productJsonContentRepository.deleteAllByProductId(foundProduct.getId());
          productRepository.delete(foundProduct);
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

  private void updateRelatedThingsOfProductFromGHContent(List<GHContent> gitHubContents, Product product) {
    if (StringUtils.isNotBlank(product.getRepositoryName())) {
      updateProductCompatibility(product);
      updateProductFromReleaseTags(product);
    } else {
      updateProductContentForNonStandardProduct(gitHubContents, product);
    }
  }

  private void updateProductContentForNonStandardProduct(List<GHContent> ghContentEntity,
      Product product) {
    ProductModuleContent initialContent = new ProductModuleContent();
    initialContent.setTag(INITIAL_VERSION);
    initialContent.setProductId(product.getId());
    ProductFactory.mappingIdForProductModuleContent(initialContent);
    product.setReleasedVersions(List.of(INITIAL_VERSION));
    product.setNewestReleaseVersion(INITIAL_VERSION);
    axonIvyProductRepoService.extractReadMeFileFromContents(product, ghContentEntity, initialContent);
    productModuleContentRepository.save(initialContent);
  }
}
