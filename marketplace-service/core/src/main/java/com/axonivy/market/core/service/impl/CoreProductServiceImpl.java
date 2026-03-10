package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductJsonContent;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.core.enums.TypeOption;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.model.VersionAndUrlModel;
import com.axonivy.market.core.repository.CoreGithubRepository;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.CoreProductMarketplaceDataService;
import com.axonivy.market.core.service.CoreProductService;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.core.utils.CoreMavenUtils;
import com.axonivy.market.core.utils.CoreVersionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.axonivy.market.core.enums.DocumentField.SHORT_DESCRIPTIONS;

import org.springframework.util.CollectionUtils;

@Log4j2
@Service
@RequiredArgsConstructor
public class CoreProductServiceImpl implements CoreProductService {
  private final CoreProductRepository coreProductRepo;
  private final CoreMetadataRepository metadataRepo;
  private final CoreProductMarketplaceDataService coreProductMarketplaceDataService;
  private final CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepository;
  private final CoreProductJsonContentRepository coreProductJsonContentRepo;
  private final CoreGithubRepository coreGithubRepository;
  private final CoreVersionService coreVersionService;

  /**
   * @deprecated This method is deprecated and will be no longer use in future release.
   *             Use {@link #findProducts(String type, String keyword, String language, Pageable pageable)} instead.
   */
  @Override
  @Deprecated
  public Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient,
      Pageable pageable) {
    final var typeOption = TypeOption.of(type);
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setListed(true);
    searchCriteria.setKeyword(keyword);
    searchCriteria.setType(typeOption);
    searchCriteria.setLanguage(Language.of(language));
    if (BooleanUtils.isTrue(isRESTClient)) {
      searchCriteria.setExcludeFields(List.of(SHORT_DESCRIPTIONS));
    }
    return coreProductRepo.searchByCriteria(searchCriteria, pageable);
  }

  @Override
  public Page<Product> findProducts(String type, String keyword, String language, Pageable pageable) {
    return findProducts(type, keyword, language, false, pageable);
  }

  @Override
  public Product fetchBestMatchProductDetail(String id, String version) {
    List<String> installableVersions = CoreVersionUtils.getInstallableVersionsFromMetadataList(
        metadataRepo.findByProductId(id));
    String bestMatchVersion = CoreVersionUtils.getBestMatchVersion(installableVersions, version);
    // Cover exception case of employee onboarding without any product.json file
    Product product;
    if (StringUtils.isBlank(bestMatchVersion)) {
      product = getProductByIdWithNewestReleaseVersion(id, false);
    } else {
      product = coreProductRepo.getProductByIdAndVersion(id, bestMatchVersion);
    }

    return Optional.ofNullable(product).map((Product productItem) -> {
      int installationCount = coreProductMarketplaceDataService.updateProductInstallationCount(id);
      productItem.setInstallationCount(installationCount);

      String compatibilityRange = getCompatibilityRange(id, productItem.getDeprecated());
      productItem.setCompatibilityRange(compatibilityRange);
      updateFocusedStatusForProduct(product);
      productItem.setBestMatchVersion(bestMatchVersion);
      return productItem;
    }).orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with id: " + id));
  }

  public Product getProductByIdWithNewestReleaseVersion(String id, Boolean isShowDevVersion) {
    List<String> versions;
    String version = StringUtils.EMPTY;

    List<MavenArtifactVersion> mavenArtifactVersions = coreMavenArtifactVersionRepository.findByProductId(id);

    if (ObjectUtils.isNotEmpty(mavenArtifactVersions)) {
      versions = CoreVersionUtils.extractAllVersions(mavenArtifactVersions, BooleanUtils.isTrue(isShowDevVersion));
      version = CollectionUtils.firstElement(versions);
    }

    // Cover exception case of employee onboarding without any product.json file
    if (StringUtils.isBlank(version)) {
      versions = CoreVersionUtils.getVersionsToDisplay(coreProductRepo.getReleasedVersionsById(id), isShowDevVersion);
      version = CollectionUtils.firstElement(versions);
    }

    var product = coreProductRepo.getProductByIdAndVersion(id, version);
    coreProductJsonContentRepo.findByProductIdAndVersionIgnoreCase(id, version).stream().map(
        ProductJsonContent::getContent).findFirst().ifPresent(
        jsonContent -> product.setMavenDropins(CoreMavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent)));
    return product;
  }

  /**
   * Retrieve the list containing all installable released versions and
   * split the versions to obtain the first prefix,then format them for compatibility range.
   * ex: 11.0+ , 10.0 - 12.0+ , ...
   */
  private String getCompatibilityRange(String productId, Boolean isDeprecatedProduct) {
    return Optional.of(coreVersionService.getInstallableVersions(productId, false, null))
        .filter(ObjectUtils::isNotEmpty)
        .map(versions -> versions.stream().map(VersionAndUrlModel::getVersion).toList())
        .map(versions -> CoreVersionUtils.getCompatibilityRangeFromVersions(versions, isDeprecatedProduct)).orElse(null);
  }

  private void updateFocusedStatusForProduct(Product product) {
    var repos = coreGithubRepository.findByNameOrProductId(EMPTY, product.getId());
    boolean isFocused = repos != null && repos.stream().anyMatch(repo -> Boolean.TRUE.equals(repo.getFocused()));
    product.setIsFocused(isFocused);
  }
}
