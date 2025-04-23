package com.axonivy.market.service.impl;

import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
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

import static com.axonivy.market.constants.CommonConstants.DOT_SEPARATOR;
import static com.axonivy.market.constants.MavenConstants.*;
import static com.axonivy.market.constants.ProductJsonConstants.DEFAULT_PRODUCT_TYPE;

@Log4j2
@AllArgsConstructor
@Service
public class ProductDependencyServiceImpl implements ProductDependencyService {
  final ProductRepository productRepository;
  final ProductDependencyRepository productDependencyRepository;
  final FileDownloadService fileDownloadService;
  final MavenArtifactVersionRepository mavenArtifactVersionRepository;

  private static Model convertPomToModel(byte[] data) {
    try (var inputStream = new ByteArrayInputStream(data)) {
      return new MavenXpp3Reader().read(inputStream);
    } catch (HttpClientErrorException | IOException | XmlPullParserException e) {
      log.error("Cannot read data by {}", e.getMessage());
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
    for (String productId : getAllListedProductIds()) {
      if (BooleanUtils.isTrue(resetSync)) {
        productDependencyRepository.deleteAllByProductId(productId);
      }
      List<MavenArtifactVersion> mavenArtifactVersions =
          mavenArtifactVersionRepository.findByProductIdOrderByAdditionalVersion(productId);
      totalSyncedProductIds = syncByMavenArtifactVersions(mavenArtifactVersions, totalSyncedProductIds);
    }
    return totalSyncedProductIds;
  }

  private int syncByMavenArtifactVersions(List<MavenArtifactVersion> mavenArtifactVersions,
      int totalSyncedProductIds) {
    for (var artifact : mavenArtifactVersions) {
      String productId = artifact.getProductId();
      String artifactId = artifact.getId().getArtifactId();
      String version = artifact.getId().getProductVersion();
      if (VersionUtils.isSnapshotVersion(version)) {
        productDependencyRepository.deleteByProductIdAndArtifactIdAndVersion(productId, artifactId, version);
      }
      ProductDependency productDependency = findProductDependencyByIds(productId, artifactId, version);
      if (productDependency == null) {
        // Missing artifacts
        productDependency = initProductDependencyData(artifact);
        // Base on version, loop the artifacts and maps its dependencies
        computeIARDependencies(artifact, productDependency);
        productDependencyRepository.save(productDependency);
        totalSyncedProductIds++;
      }
    }
    return totalSyncedProductIds;
  }

  private ProductDependency findProductDependencyByIds(String productId, String artifactId, String version) {
    List<ProductDependency> productDependencies = productDependencyRepository.findByProductIdAndArtifactIdAndVersion(
        productId, artifactId, version);
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

  private void computeIARDependencies(MavenArtifactVersion artifact, ProductDependency mavenDependency) {
    List<Dependency> dependencyModels = extractMavenPOMDependencies(artifact.getDownloadUrl());
    log.info("Collect IAR dependencies for requested artifact {}", artifact.getId().getArtifactId());
    collectMavenDependenciesFor(artifact.getProductId(), artifact.getId().getProductVersion(),
        mavenDependency.getDependencies(), dependencyModels);
  }

  private void collectMavenDependenciesFor(String productId, String version,
      List<ProductDependency> productDependencies, List<Dependency> dependencyModels) {
    for (var dependencyModel : dependencyModels) {
      String artifactId = dependencyModel.getArtifactId();
      ProductDependency dependency = findProductDependencyByIds(productId, artifactId, version);
      if (dependency == null) {
        dependency = ProductDependency.builder().productId(productId).artifactId(artifactId).version(version).build();
      }
      MavenArtifactVersion dependencyArtifact = findDownloadURLForDependency(productId, artifactId, version);
      if (dependencyArtifact != null && StringUtils.isNotBlank(dependencyArtifact.getDownloadUrl())) {
        dependency.setDownloadUrl(dependencyArtifact.getDownloadUrl());
        productDependencies.add(dependency);
        // Check does dependency artifact has IAR lib, e.g: portal
        log.info("Collect nested IAR dependencies for artifact {}", dependencyArtifact.getId().getArtifactId());
        List<Dependency> dependenciesOfParent = extractMavenPOMDependencies(dependencyArtifact.getDownloadUrl());
        collectMavenDependenciesFor(productId, version, productDependencies, dependenciesOfParent);
      }
    }
  }

  private List<String> getAllListedProductIds() {
    return productRepository.findAll().stream()
        .filter(product -> Boolean.FALSE != product.getListed())
        .map(Product::getId).toList();
  }

  private MavenArtifactVersion findDownloadURLForDependency(String productId, String artifactId, String version) {
    List<MavenArtifactVersion> mavenArtifactVersions =
        mavenArtifactVersionRepository.findByArtifactIdAndVersion(productId, artifactId, version);
    return ObjectUtils.isEmpty(mavenArtifactVersions) ? null : mavenArtifactVersions.get(0);
  }

  private List<Dependency> extractMavenPOMDependencies(String downloadUrl) {
    List<Dependency> dependencies = new ArrayList<>();
    byte[] location = downloadPOMFileFromMaven(downloadUrl);
    Model mavelModel = convertPomToModel(location);
    if (mavelModel != null) {
      dependencies = mavelModel.getDependencies().stream()
          .filter(dependency -> DEFAULT_PRODUCT_TYPE.equals(dependency.getType()))
          .toList();
    }
    return dependencies;
  }

  private byte[] downloadPOMFileFromMaven(String downloadUrl) {
    if (StringUtils.isNotBlank(downloadUrl)) {
      downloadUrl = downloadUrl.replaceFirst(DEFAULT_IVY_MAVEN_BASE_URL, DEFAULT_IVY_MIRROR_MAVEN_BASE_URL);
      downloadUrl = downloadUrl.replace(DOT_SEPARATOR.concat(DEFAULT_PRODUCT_TYPE), DOT_SEPARATOR.concat(POM));
      try {
        return fileDownloadService.downloadFile(downloadUrl);
      } catch (Exception e) {
        log.error("Exception during download pom file by URL {}", downloadUrl);
      }
    }
    return new byte[0];
  }
}
