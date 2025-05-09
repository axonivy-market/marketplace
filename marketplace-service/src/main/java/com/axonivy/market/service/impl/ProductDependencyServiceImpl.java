package com.axonivy.market.service.impl;

import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ProductDependencyService;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.axonivy.market.constants.CommonConstants.DOT_SEPARATOR;
import static com.axonivy.market.constants.MavenConstants.*;
import static com.axonivy.market.constants.ProductJsonConstants.DEFAULT_PRODUCT_TYPE;

@Log4j2
@AllArgsConstructor
@Service
public class ProductDependencyServiceImpl implements ProductDependencyService {
  static final int SAFE_THRESHOLD = 11;
  final ProductRepository productRepository;
  final ProductDependencyRepository productDependencyRepository;
  final FileDownloadService fileDownloadService;
  final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  final MetadataRepository metadataRepository;

  private static Model convertPomToModel(byte[] data) throws IOException, XmlPullParserException, NullPointerException {
    try (var inputStream = new ByteArrayInputStream(data)) {
      return new MavenXpp3Reader().read(inputStream);
    }
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
  public int syncIARDependenciesForProducts(Boolean resetSync, String productId) {
    int totalSyncedProductIds = 0;
    if (StringUtils.isNotBlank(productId)) {
      totalSyncedProductIds += syncByMavenArtifactVersions(productId, resetSync);
    } else {
      for (String id : getAllListedProductIds()) {
        totalSyncedProductIds += syncByMavenArtifactVersions(id, resetSync);
      }
    }
    return totalSyncedProductIds;
  }

  private int syncByMavenArtifactVersions(String id, Boolean resetSync) {
    int totalSyncedProductIds = 0;
    if (BooleanUtils.isTrue(resetSync)) {
      productDependencyRepository.deleteAllByProductId(id);
    }
    for (var artifact : mavenArtifactVersionRepository.findByProductIdOrderByAdditionalVersion(id)) {
      String productId = artifact.getProductId();
      String artifactId = artifact.getId().getArtifactId();
      String version = artifact.getId().getProductVersion();
      if (VersionUtils.isSnapshotVersion(version)) {
        productDependencyRepository.deleteByProductIdAndArtifactIdAndVersion(productId, artifactId, version);
      }
      ProductDependency productDependency = findProductDependencyByIds(productId, artifactId, version);
      if (productDependency == null) { // Is missing artifacts ?
        productDependency = initProductDependencyData(artifact);
        try {
          // Base on version, loop the artifacts and maps its dependencies
          computeIARDependencies(artifact, productDependency);
          productDependencyRepository.save(productDependency);
          totalSyncedProductIds++;
        } catch (Exception e) {
          log.error("Got issue during sync data for {} - {} - {} - {}", productId, artifact, version, e.getMessage());
        }
      }
    }
    return totalSyncedProductIds;
  }

  private ProductDependency findProductDependencyByIds(String productId, String artifactId, String version) {
    var productDependencies = productDependencyRepository.findByProductIdAndArtifactIdAndVersion(productId, artifactId,
        version);
    return ObjectUtils.isEmpty(productDependencies) ? null : productDependencies.get(0);
  }

  private ProductDependency initProductDependencyData(MavenArtifactVersion mavenArtifactVersion) {
    return ProductDependency.builder().productId(mavenArtifactVersion.getProductId())
        .artifactId(mavenArtifactVersion.getId().getArtifactId())
        .version(mavenArtifactVersion.getId().getProductVersion())
        .downloadUrl(mavenArtifactVersion.getDownloadUrl())
        .dependencies(new ArrayList<>())
        .build();
  }

  private void computeIARDependencies(MavenArtifactVersion artifact,
      ProductDependency mavenDependency) throws Exception {
    List<Dependency> dependencyModels = extractMavenPOMDependencies(artifact.getDownloadUrl());
    if (ObjectUtils.isEmpty(dependencyModels)) {
      log.info("No dependency was found for requested artifact {}", artifact.getId().getArtifactId());
      return;
    }
    log.info("Collect IAR dependencies for requested artifact {}", artifact.getId().getArtifactId());
    int totalDependencyLevels = 0;
    collectMavenDependenciesFor(artifact.getId().getProductVersion(), mavenDependency.getDependencies(),
        dependencyModels, totalDependencyLevels);
  }

  private void collectMavenDependenciesFor(String version, List<ProductDependency> productDependencies,
      List<Dependency> dependencyModels, int totalDependencyLevels) throws Exception {
    if (totalDependencyLevels > SAFE_THRESHOLD) {
      throw new MarketException(ErrorCode.INTERNAL_EXCEPTION.getCode(), ErrorCode.INTERNAL_EXCEPTION.getHelpText());
    }
    for (var dependencyModel : dependencyModels) {
      String dependencyVersion = VersionFactory.resolveVersion(dependencyModel.getVersion(), version);
      Metadata dependencyMetadata = getMetadataByVersion(dependencyModel, dependencyVersion);
      ProductDependency dependency = findProductDependencyByIds(dependencyMetadata.getProductId(),
          dependencyMetadata.getArtifactId(), dependencyVersion);
      if (dependency == null) {
        dependency = ProductDependency.builder().productId(dependencyMetadata.getProductId()).artifactId(
            dependencyMetadata.getArtifactId()).version(dependencyVersion).build();
      }
      MavenArtifactVersion dependencyArtifact = findDownloadURLForDependency(dependencyMetadata.getProductId(),
          dependencyMetadata.getArtifactId(), dependencyVersion);
      dependency.setDownloadUrl(dependencyArtifact.getDownloadUrl());
      productDependencies.add(dependency);
      // Check does dependency artifact has IAR lib, e.g: portal
      log.info("Collect nested IAR dependencies for artifact {}", dependencyArtifact.getId().getArtifactId());
      totalDependencyLevels++;
      List<Dependency> dependenciesOfParent = extractMavenPOMDependencies(dependencyArtifact.getDownloadUrl());
      collectMavenDependenciesFor(version, productDependencies, dependenciesOfParent, totalDependencyLevels);
    }
  }

  private Metadata getMetadataByVersion(Dependency dependencyModel, String version) {
    List<Metadata> existingMetadata = metadataRepository.findByGroupIdAndArtifactId(dependencyModel.getGroupId(),
        dependencyModel.getArtifactId());
    return existingMetadata.stream().filter(meta -> meta.getVersions().contains(version)).findAny()
        .orElse(null);
  }

  private List<String> getAllListedProductIds() {
    return productRepository.findAll().stream()
        .filter(product -> Boolean.FALSE != product.getListed())
        .map(Product::getId).toList();
  }

  private MavenArtifactVersion findDownloadURLForDependency(String productId, String artifactId, String version) {
    var mavenArtifactVersions = mavenArtifactVersionRepository.findByProductIdAndArtifactIdAndVersion(productId,
        artifactId, version);
    var dependencyArtifact = ObjectUtils.isEmpty(mavenArtifactVersions) ? null : mavenArtifactVersions.get(0);
    Objects.requireNonNull(dependencyArtifact, "Cannot found the dependency artifact of " + artifactId);
    ObjectUtils.requireNonEmpty(dependencyArtifact.getDownloadUrl(), "Invalid download URL for " + artifactId);
    return dependencyArtifact;
  }

  private List<Dependency> extractMavenPOMDependencies(String downloadUrl)
      throws IOException, XmlPullParserException, NullPointerException, HttpClientErrorException {
    byte[] location = downloadPOMFileFromMaven(downloadUrl);
    Model mavelModel = convertPomToModel(location);
    return mavelModel.getDependencies().stream()
        .filter(dependency -> DEFAULT_PRODUCT_TYPE.equals(dependency.getType()))
        .toList();
  }

  private byte[] downloadPOMFileFromMaven(String downloadUrl) throws HttpClientErrorException {
    ObjectUtils.requireNonEmpty(downloadUrl, "Download URL must not be null");
    var changeToMirrorRepo = downloadUrl.replaceFirst(DEFAULT_IVY_MAVEN_BASE_URL, DEFAULT_IVY_MIRROR_MAVEN_BASE_URL);
    var pomURL = changeToMirrorRepo.replace(DOT_SEPARATOR.concat(DEFAULT_PRODUCT_TYPE), DOT_SEPARATOR.concat(POM));
    return fileDownloadService.downloadFile(pomURL);
  }
}
