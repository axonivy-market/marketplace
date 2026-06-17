package com.axonivy.market.service.impl;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductCustomSort;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.enums.SortOption;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.enums.PullRequestAction;
import com.axonivy.market.enums.RepositoryAction;
import com.axonivy.market.exceptions.model.ArchiveNotAllowedException;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.AlternativeExtensionData;
import com.axonivy.market.model.DeprecationRequest;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.model.ProductDeprecationProjection;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.MavenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHPullRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductMarketplaceDataServiceImpl implements ProductMarketplaceDataService {

  private static final int MIN_RANDOM_INSTALLATION_COUNT = 20;
  private static final int MAX_RANDOM_INSTALLATION_COUNT = 50;
  private final ProductMarketplaceDataRepository productMarketplaceDataRepo;
  private final ProductCustomSortRepository productCustomSortRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final ProductRepository productRepo;
  private final ProductDesignerInstallationRepository productDesignerInstallationRepo;
  private final FileDownloadService fileDownloadService;
  private final GitHubService gitHubService;
  private final AxonIvyClient axonIvyClient;
  private final SecureRandom random = new SecureRandom();

  @Override
  public void addCustomSortProduct(ProductCustomSortRequest customSort) {
    SortOption.of(customSort.getRuleForRemainder());

    var productCustomSort = new ProductCustomSort(customSort.getRuleForRemainder());
    productCustomSortRepo.deleteAll();
    productMarketplaceDataRepo.resetCustomOrderForAllProducts();
    productCustomSortRepo.save(productCustomSort);
    productMarketplaceDataRepo.saveAll(refineOrderedListOfProductsInCustomSort(customSort.getOrderedListOfProducts()));
  }

  @Override
  public ProductCustomSortRequest getCustomSortProducts() {
    List<String> orderedProducts = productMarketplaceDataRepo.findByCustomOrderIsNotNullOrderByCustomOrderDesc()
        .stream()
        .map(ProductMarketplaceData::getId)
        .toList();

    String remainderRule = productCustomSortRepo.findAll().stream().findFirst()
        .map(ProductCustomSort::getRuleForRemainder)
        .orElse(SortOption.ALPHABETICALLY.getOption());

    return new ProductCustomSortRequest(orderedProducts, remainderRule);
  }

  public List<ProductMarketplaceData> refineOrderedListOfProductsInCustomSort(
      Collection<String> orderedListOfProducts) {
    List<ProductMarketplaceData> productEntries = new ArrayList<>();

    int descendingOrder = orderedListOfProducts.size();
    for (String productId : orderedListOfProducts) {
      validateProductExists(productId);
      var productMarketplaceData = getProductMarketplaceData(productId);

      int currentOrder = descendingOrder;
      descendingOrder--;
      productMarketplaceData.setCustomOrder(currentOrder);
      productEntries.add(productMarketplaceData);
    }
    return productEntries;
  }

  @Override
  public int updateInstallationCountForProduct(String productId, String designerVersion) {
    validateProductExists(productId);
    var productMarketplaceData = getProductMarketplaceData(productId);

    log.info("Increase installation count for product {} By Designer Version {}", productId, designerVersion);
    if (StringUtils.isNotBlank(designerVersion)) {
      productDesignerInstallationRepo.increaseInstallationCountForProductByDesignerVersion(productId, designerVersion);
    }

    log.info("updating installation count for product {}", productId);
    if (BooleanUtils.isTrue(productMarketplaceData.getSynchronizedInstallationCount())) {
      return productMarketplaceDataRepo.increaseInstallationCount(productId);
    }
    int installationCount = generateRandomInstallationCount();
    return productMarketplaceDataRepo.updateInitialCount(productId, installationCount + 1);
  }

  public int generateRandomInstallationCount() {
    return random.nextInt(MIN_RANDOM_INSTALLATION_COUNT, MAX_RANDOM_INSTALLATION_COUNT);
  }

  @Override
  public ProductMarketplaceData updateProductInstallationCount(String id) {
    var productMarketplaceData = getProductMarketplaceData(id);
    if (BooleanUtils.isNotTrue(productMarketplaceData.getSynchronizedInstallationCount())) {
      int installationCount = productMarketplaceDataRepo.updateInitialCount(id, generateRandomInstallationCount());
      productMarketplaceData.setInstallationCount(installationCount);
    }
    return productMarketplaceData;
  }

  @Override
  public ProductMarketplaceData getProductMarketplaceData(String productId) {
    return productMarketplaceDataRepo.findById(productId).orElse(
        ProductMarketplaceData.builder().id(productId).build());
  }

  @Override
  public Integer getInstallationCount(String id) {
    return productMarketplaceDataRepo.findById(id)
        .map(ProductMarketplaceData::getInstallationCount)
        .orElse(0);
  }

  @Override
  public ResponseEntity<Resource> getProductArtifactStream(String productId, String artifactId, String version) {
    var mavenArtifactVersions = mavenArtifactVersionRepo.findByProductIdAndArtifactIdAndVersion(productId, artifactId,
        version);
    if (CollectionUtils.isEmpty(mavenArtifactVersions)) {
      return null;
    }
    String downloadUrl = mavenArtifactVersions.getFirst().getDownloadUrl();
    return fileDownloadService.fetchUrlResource(downloadUrl);
  }

  private void validateProductExists(String productId) throws NotFoundException {
    if (productRepo.findById(productId).isEmpty()) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
    }
  }

  @Override
  public OutputStream buildArtifactStreamFromResource(String productId, Resource resource, OutputStream outputStream) {
    try (var inputStream = resource.getInputStream()) {
      FileUtils.writeBlobAsChunks(inputStream, outputStream);
      outputStream.flush();
      int count = updateInstallationCountForProduct(productId, null);
      log.debug("File {} downloaded, installation count incremented to {}", productId, count);
    } catch (IOException e) {
      log.error("Error streaming file for product {}: {}", productId, e.getMessage(), e);
    }
    return outputStream;
  }

  @Transactional(rollbackOn = IOException.class)
  @Override
  public String updateSuccessorForProduct(String productId, DeprecationRequest request)
      throws IOException {
    AlternativeExtensionData extensionData = null;
    ProductMarketplaceData productMarketplaceData = getProductMarketplaceData(productId);

    if (productMarketplaceData != null) {
      extensionData = getSuccessorAndAlternativeExtensionForAction(productMarketplaceData, request);
      Date deprecationDate = Optional.ofNullable(request.getDeprecationDate()).orElse(new Date());
      productMarketplaceData.setDeprecationDate(deprecationDate);
      productMarketplaceData.setDeprecationRequester(request.getDeprecationRequester());
      productMarketplaceData.setSuccessor(request.getSuccessorUrl());
      productMarketplaceData.setAlternativeExtension(request.getAlternativeExtension());
      productMarketplaceDataRepo.save(productMarketplaceData);
    }

    String pullRequestUrl = null;
    Product product = productRepo.findById(productId).orElse(null);
    if (product != null) {
      if (extensionData != null) {
        String nextMajorVersion = findNextMajorVersion(product.getNewestReleaseVersion());
        extensionData.setDeprecatedVersionFrom(nextMajorVersion);
      }
      product.setDeprecated(request.getIsDeprecated());
      // When we deprecate/undeprecate It should be null always. Updating this field will be executed in method
      // archiveOrUnarchiveRepository
      product.setIsArchived(null);
      pullRequestUrl = handlePullRequest(product, request, extensionData);
      productRepo.save(product);
    }
    return pullRequestUrl;
  }

  private String findNextMajorVersion(String newestReleaseVersion) {
    List<String> allVersion = axonIvyClient.getAllVersions();
    return MavenUtils.findNextByMajorPrefix(newestReleaseVersion, allVersion);
  }

  private AlternativeExtensionData getSuccessorAndAlternativeExtensionForAction(
      ProductMarketplaceData productMarketplaceData, DeprecationRequest request) {
    if (request.getPullRequestAction() == PullRequestAction.REMOVE) {
      return AlternativeExtensionData.builder()
          .alternativeExtension(productMarketplaceData.getAlternativeExtension())
          .successorUrl(productMarketplaceData.getSuccessor())
          .build();
    }
    return AlternativeExtensionData.builder()
        .alternativeExtension(request.getAlternativeExtension())
        .successorUrl(request.getSuccessorUrl())
        .build();
  }


  @Override
  public List<ProductDeprecationProjection> getProductIdsByDeprecated(Boolean isDeprecated) {
    return productMarketplaceDataRepo.findProductIdsByDeprecated(isDeprecated);
  }

  @Override
  @Transactional(rollbackOn = IOException.class)
  public void archiveOrUnarchiveRepository(String productId, RepositoryAction action) throws IOException {
    Product product = productRepo.findById(productId).orElseThrow(
        () -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND.getCode(), "Product not found: " + productId));

    if (action == RepositoryAction.UNARCHIVE) {
      gitHubService.unArchivedTheRepository(product.getRepositoryName());
    } else {
      boolean hasDeprecationWarning = gitHubService.hasDeprecationWarningInReadme(product.getRepositoryName());
      if (!hasDeprecationWarning) {
        throw new ArchiveNotAllowedException(ErrorCode.ARCHIVE_NOT_ALLOWED,
            "Cannot archive repository '" + productId + "': README does not contain deprecation warning");
      }
      gitHubService.archiveTheRepository(product.getRepositoryName());
    }

    product.setIsArchived(action == RepositoryAction.ARCHIVE ? true : null);
    productRepo.save(product);
  }

  private String handlePullRequest(Product product, DeprecationRequest request,
      AlternativeExtensionData extensionData) throws IOException {
    if (!request.getIsAddReadme() || request.getPullRequestAction() == null || StringUtils.isBlank(
        product.getRepositoryName())) {
      return null;
    }
    GHPullRequest pullRequest = gitHubService.updateReadmeForSuccessorNotes(
        product.getRepositoryName(), request.getPullRequestAction(), extensionData);

    return Optional.ofNullable(pullRequest)
        .map(GHPullRequest::getHtmlUrl)
        .map(Object::toString)
        .orElse(null);
  }
}
