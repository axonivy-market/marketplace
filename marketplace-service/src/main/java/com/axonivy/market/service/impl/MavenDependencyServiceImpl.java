package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.MavenDependency;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.MavenDependencyService;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.DirectoryConstants.*;
import static com.axonivy.market.constants.MavenConstants.*;

@Log4j2
@AllArgsConstructor
@Service
public class MavenDependencyServiceImpl implements MavenDependencyService {
  final ProductRepository productRepository;
  final ProductDependencyRepository productDependencyRepository;
  final FileDownloadService fileDownloadService;
  final MavenArtifactVersionRepository mavenArtifactVersionRepository;

  private static Model convertPomToModel(File pomFile) {
    try (var inputStream = new FileInputStream(pomFile)) {
      return new MavenXpp3Reader().read(inputStream);
    } catch (HttpClientErrorException | IOException | XmlPullParserException e) {
      log.error("Cannot read data from {} by {}", pomFile.toPath(), e.getMessage());
    }
    return null;
  }

  /**
   * The job will find all data in {@link MavenArtifactVersion} table:
   * * Base on version of product and then loop the artifacts:
   * ** Download the artifact from maven repo by {@link MavenArtifactVersion#getDownloadUrl()} to collect
   * dependencies in type IAR in pom.xml file.
   * ** If found, system will find that dependency by ID in the MavenArtifactVersion table:
   * *** Must loop over ProductArtifactsByVersion and AdditionalArtifactsByVersion property
   * * At the end, system will save a new collection to DB, that is {@link ProductDependency}:
   * ** Include all required IAR dependencies of the request product artifact
   *
   * @param resetSync is indicator that should system delete data first then sync everything from scratch
   * @return total product synced
   */
  @Override
  public int syncIARDependenciesForProducts(Boolean resetSync) {
    int totalSyncedProductIds = 0;
    for (String productId : getMissingProductIds(resetSync)) {
      List<MavenArtifactVersion> mavenArtifactVersions = mavenArtifactVersionRepository.findByProductId(productId)
          .stream().sorted(sortByAdditionalVersion()).toList();

      // If no data in MavenArtifactVersion table then skip this product
      if (ObjectUtils.isEmpty(mavenArtifactVersions)) {
        continue;
      }
      ProductDependency productDependency = initProductDependencyData(productId);
      // Base on version, loop the artifacts and maps its dependencies
      collectIARDependenciesByArtifactVersion(productId, mavenArtifactVersions, productDependency.getDependenciesOfArtifact());
      productDependencyRepository.save(productDependency);
      totalSyncedProductIds++;
    }
    return totalSyncedProductIds;
  }

  private ProductDependency initProductDependencyData(String productId) {
    ProductDependency productDependency = productDependencyRepository.findByIdWithDependencies(productId);
    if (productDependency == null) {
      productDependency = ProductDependency.builder().dependenciesOfArtifact(new ArrayList<>()).productId(productId)
          .build();
    }
    return productDependency;
  }

  private void collectIARDependenciesByArtifactVersion(String productId,
      List<MavenArtifactVersion> productArtifactsByVersion, List<MavenDependency> dependenciesOfArtifact) {
    List<String> existedVersions = dependenciesOfArtifact.stream().map(MavenDependency::getVersion).toList();
    productArtifactsByVersion.stream()
        .filter(Objects::nonNull)
        .filter(filterNotDocOrZipArtifact())
        .filter(filterSnapOrNotExistedVersions(existedVersions))
        .forEach(mavenArtifactVersion -> {
          var proceedVersion = mavenArtifactVersion.getId().getProductVersion();
          if (VersionUtils.isSnapshotVersion(proceedVersion)) {
            dependenciesOfArtifact.removeIf(filterByVersionAndArtifactId(mavenArtifactVersion, proceedVersion));
          }
          MavenDependency mavenDependency = computeIARDependencies(productId, proceedVersion, mavenArtifactVersion);
          dependenciesOfArtifact.add(mavenDependency);
    });
  }

  private static Predicate<MavenDependency> filterByVersionAndArtifactId(MavenArtifactVersion mavenArtifactVersion,
      String proceedVersion) {
    return mavenDependency -> StringUtils.equals(mavenDependency.getVersion(), proceedVersion)
        && StringUtils.equals(mavenDependency.getArtifactId(), mavenArtifactVersion.getId().getArtifactId());
  }

  private Predicate<? super MavenArtifactVersion> filterSnapOrNotExistedVersions(List<String> existedVersions) {
    return artifact -> VersionUtils.isSnapshotVersion(artifact.getId().getProductVersion())
        || !existedVersions.contains(artifact.getId().getProductVersion());
  }

  private static Predicate<MavenArtifactVersion> filterNotDocOrZipArtifact() {
    return artifact -> !StringUtils.endsWith(artifact.getId().getArtifactId(), DOC)
        && !StringUtils.endsWith(artifact.getDownloadUrl(), DEFAULT_PRODUCT_FOLDER_TYPE);
  }

  private MavenDependency computeIARDependencies(String productId, String version, MavenArtifactVersion artifact) {
    var artifactId = artifact.getId().getArtifactId();
    List<Dependency> dependencyModels = extractMavenPOMDependencies(artifact);
    List<MavenDependency> mavenDependencies = new ArrayList<>();
    log.info("Collect IAR dependencies for requested artifact {}", artifactId);
    collectMavenDependenciesFor(productId, version, mavenDependencies, dependencyModels);

    return MavenDependency.builder()
        .version(version)
        .downloadUrl(artifact.getDownloadUrl())
        .dependencies(mavenDependencies)
        .artifactId(artifactId)
        .build();
  }

  private void collectMavenDependenciesFor(String productId, String version, List<MavenDependency> mavenDependencies,
      List<Dependency> dependencyModels) {
    for (var dependencyModel : dependencyModels) {
      MavenDependency dependency = MavenDependency.builder().artifactId(dependencyModel.getArtifactId()).build();
      MavenArtifactVersion dependencyArtifact = findDownloadURLForDependency(productId, version, dependencyModel);
      if (dependencyArtifact != null && StringUtils.isNotBlank(dependencyArtifact.getDownloadUrl())) {
        dependency.setDownloadUrl(dependencyArtifact.getDownloadUrl());
        mavenDependencies.add(dependency);
        // Check does dependency artifact has IAR lib, e.g: portal
        log.info("Collect nested IAR dependencies for artifact {}", dependencyArtifact.getId().getArtifactId());
        List<Dependency> dependenciesOfParent = extractMavenPOMDependencies(dependencyArtifact);
        collectMavenDependenciesFor(productId, version, mavenDependencies, dependenciesOfParent);
      }
    }
  }

  private List<String> getMissingProductIds(boolean resetSync) {
    List<Product> availableProducts = getAllListedProductIds();
    List<ProductDependency> syncedProducts = new ArrayList<>();
    if (resetSync) {
      log.warn("Remove all ProductDependency documents due to force sync");
      productDependencyRepository.deleteAll();
    } else {
      syncedProducts = productDependencyRepository.findAllWithDependencies();
    }

    // Subtract existing product id
    List<String> availableProductIds = availableProducts.stream().map(Product::getId).toList();
    List<String> syncedProductIds = syncedProducts.stream().map(ProductDependency::getProductId).toList();
    List<String> missingProductIds = new ArrayList<>(availableProductIds);
    missingProductIds.removeAll(syncedProductIds);
    missingProductIds.addAll(collectProductMismatchVersion(availableProducts, syncedProducts));
    return missingProductIds.stream().distinct().toList();
  }

  private List<String> collectProductMismatchVersion(List<Product> availableProducts,
      List<ProductDependency> syncedProducts) {
    List<String> mismatchVersionIds = new ArrayList<>();
    for (var product : availableProducts) {
      List<String> releasedVersions = product.getReleasedVersions();
      // If product has any SNAPSHOT version, then must sync data for it
      if (isContainAnySnapshotVersion(releasedVersions).isPresent()) {
        mismatchVersionIds.add(product.getId());
      } else {
        filterProductBySyncedVersionsAndReleaseVersions(syncedProducts, product, releasedVersions,
            mismatchVersionIds);
      }
    }
    return mismatchVersionIds;
  }

  private static void filterProductBySyncedVersionsAndReleaseVersions(List<ProductDependency> syncedProducts,
      Product product, List<String> releasedVersions, List<String> mismatchingVersionIds) {
    syncedProducts.stream()
        .filter(productDependency -> productDependency.getProductId().equals(product.getId()))
        .findAny().ifPresent(syncedProduct -> {
          var syncedVersions = syncedProduct.getDependenciesOfArtifact().stream()
              .map(MavenDependency::getVersion).collect(Collectors.toSet());
          if (VersionUtils.removeSyncedVersionsFromReleasedVersions(releasedVersions, syncedVersions).isEmpty()) {
            mismatchingVersionIds.add(syncedProduct.getProductId());
          }
        });
  }

  private Optional<String> isContainAnySnapshotVersion(List<String> versions) {
    return Stream.ofNullable(versions).flatMap(List::stream).filter(VersionUtils::isSnapshotVersion).findAny();
  }

  private List<Product> getAllListedProductIds() {
    return productRepository.findAll().stream()
        .filter(product -> Boolean.FALSE != product.getListed())
        .toList();
  }

  private MavenArtifactVersion findDownloadURLForDependency(String productId, String version, Dependency dependency) {
    String requestArtifactId = dependency.getArtifactId();
    List<MavenArtifactVersion> mavenArtifactVersion = mavenArtifactVersionRepository.findByProductId(productId)
        .stream().sorted(sortByAdditionalVersion()).toList();
    if (ObjectUtils.isEmpty(mavenArtifactVersion)) {
      return null;
    }

    return filterMavenArtifactVersionByArtifactId(version, requestArtifactId, mavenArtifactVersion);
  }

  private static Comparator<MavenArtifactVersion> sortByAdditionalVersion() {
    return Comparator.comparing(artifactModel -> artifactModel.getId().isAdditionalVersion());
  }

  private MavenArtifactVersion filterMavenArtifactVersionByArtifactId(String version, String compareArtifactId,
      List<MavenArtifactVersion> artifactsByVersion) {
    return artifactsByVersion.stream()
        .filter(artifact -> artifact.getId().getProductVersion().equals(version) &&
            artifact.getId().getArtifactId().equals(compareArtifactId))
        .findFirst()
        .orElse(null);
  }

  private List<Dependency> extractMavenPOMDependencies(MavenArtifactVersion artifact) {
    List<Dependency> dependencies = new ArrayList<>();
    String location = downloadArtifactToLocal(artifact);
    if (StringUtils.isNotBlank(location)) {
      Model mavelModel = convertPomToModel(new File(location + SLASH + POM));
      if (mavelModel != null) {
        dependencies = mavelModel.getDependencies().stream()
            .filter(dependency -> ProductJsonConstants.DEFAULT_PRODUCT_TYPE.equals(dependency.getType()))
            .toList();
      }
      // Delete work folder
      fileDownloadService.deleteDirectory(Path.of(location));
    }
    return dependencies;
  }

  private String downloadArtifactToLocal(MavenArtifactVersion artifact) {
    var location = "";
    try {
      var unzipPath = String.join(File.separator, DATA_DIR, WORK_DIR, MAVEN_DIR, artifact.getId().getArtifactId());
      location = fileDownloadService.downloadAndUnzipFile(artifact.getDownloadUrl(),
          DownloadOption.builder().isForced(true).workingDirectory(unzipPath).shouldGrantPermission(false).build());
    } catch (Exception e) {
      log.error("Exception during unzip");
    }
    return location;
  }
}
