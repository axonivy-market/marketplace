package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.bo.MavenDependency;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.MavenDependencyService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import java.util.*;
import java.util.function.Predicate;

import static com.axonivy.market.constants.CommonConstants.*;
import static com.axonivy.market.constants.DirectoryConstants.*;
import static com.axonivy.market.constants.MavenConstants.*;

@Log4j2
@AllArgsConstructor
@Service
public class MavenDependencyServiceImpl implements MavenDependencyService {
  final ProductRepository productRepository;
  final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  final ProductDependencyRepository productDependencyRepository;
  final FileDownloadService fileDownloadService;

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
   ** Base on version of product and then loop the artifacts:
   *  ** Download the artifact from maven repo by {@link MavenArtifactModel#getDownloadUrl()} to collect
   *    dependencies in type IAR in pom.xml file.
   *  ** If found, system will find that dependency by ID in the MavenArtifactVersion table:
   *     *** Must loop over ProductArtifactsByVersion and AdditionalArtifactsByVersion property
   ** At the end, system will save a new collection to DB, that is {@link ProductDependency}:
   *  ** Include all required IAR dependencies of the request product artifact
   * @param resetSync is indicator that should system delete data first then sync everything from scratch
   * @return total product synced
   */
  @Override
  public int syncIARDependenciesForProducts(Boolean resetSync) {
    int totalSyncedProductIds = 0;
    for (var productId : getMissingProductIds(resetSync)) {
      MavenArtifactVersion mavenArtifactVersion = mavenArtifactVersionRepository.findById(productId).orElse(null);
      // If no data in MavenArtifactVersion table then skip this product
      if (mavenArtifactVersion == null) {
        continue;
      }
      List<MavenDependency> dependenciesOfArtifact = new ArrayList<>();
      // Base on version, loop the artifacts and maps its dependencies
      // Loops in ProductArtifactsByVersion
      collectIARDependenciesByArtifactVersion(productId, mavenArtifactVersion.getProductArtifactsByVersion(),
          dependenciesOfArtifact);
      // Loops in AdditionalArtifactsByVersion
      collectIARDependenciesByArtifactVersion(productId, mavenArtifactVersion.getAdditionalArtifactsByVersion(),
          dependenciesOfArtifact);

      ProductDependency productDependency = ProductDependency.builder()
          .productId(productId)
          .dependenciesOfArtifactTest(dependenciesOfArtifact)
          .build();

      productDependencyRepository.save(productDependency);
      totalSyncedProductIds++;
    }
    return totalSyncedProductIds;
  }

  private void collectIARDependenciesByArtifactVersion(String productId,
      List<MavenArtifactModel> productArtifactsByVersion,
      List<MavenDependency> dependenciesOfArtifact) {

    List<MavenArtifactModel> mavenArtifactModels = productArtifactsByVersion.stream()
        .filter(Objects::nonNull)
        .filter(filterNotDocArtifactOrZipArtifact())
        .toList();

    for (MavenArtifactModel mavenArtifactModel : mavenArtifactModels) {
      computeIARDependencies(
          productId,
          mavenArtifactModel.getProductVersion(),
          mavenArtifactModel,
          dependenciesOfArtifact
      );
    }
  }


  private static Predicate<MavenArtifactModel> filterNotDocArtifactOrZipArtifact() {
    return artifact -> !StringUtils.endsWith(artifact.getArtifactId(), DOC)
        && !StringUtils.endsWith(artifact.getDownloadUrl(), DEFAULT_PRODUCT_FOLDER_TYPE);
  }

  private void computeIARDependencies(String productId, String version, MavenArtifactModel artifact,
      List<MavenDependency> dependenciesOfArtifact) {
    var artifactId = artifact.getArtifactId();
    List<Dependency> dependencyModels = extractMavenPOMDependencies(artifact);
    List<MavenDependency> mavenDependencies = new ArrayList<>();
    log.info("Collect IAR dependencies for requested artifact {}", artifactId);
    collectMavenDependenciesFor(productId, version, mavenDependencies, dependencyModels);

    MavenDependency artifactDependency = MavenDependency.builder()
        .version(version)
        .downloadUrl(artifact.getDownloadUrl())
        .dependencies(mavenDependencies)
        .artifactId(artifactId)
        .build();

    dependenciesOfArtifact.add(artifactDependency);
  }

  private void collectMavenDependenciesFor(String productId, String version, List<MavenDependency> mavenDependencies,
      List<Dependency> dependencyModels) {
    for (var dependencyModel : dependencyModels) {
      MavenDependency dependency = MavenDependency.builder().artifactId(dependencyModel.getArtifactId()).build();
      MavenArtifactModel dependencyArtifact = findDownloadURLForDependency(productId, version, dependencyModel);
      if (dependencyArtifact != null && StringUtils.isNotBlank(dependencyArtifact.getDownloadUrl())) {
        dependency.setDownloadUrl(dependencyArtifact.getDownloadUrl());
        mavenDependencies.add(dependency);
        // Check does dependency artifact has IAR lib, e.g: portal
        log.info("Collect nested IAR dependencies for artifact {}", dependencyArtifact.getArtifactId());
        List<Dependency> dependenciesOfParent = extractMavenPOMDependencies(dependencyArtifact);
        collectMavenDependenciesFor(productId, version, mavenDependencies, dependenciesOfParent);
      }
    }
  }

  private List<String> getMissingProductIds(boolean resetSync) {
    List<String> availableProductIds = productRepository.findAllProductIds();
    List<String> syncedProductIds = new ArrayList<>();
    if (resetSync) {
      log.warn("Remove all ProductDependency documents due to force sync");
      productDependencyRepository.deleteAll();
    } else {
      syncedProductIds = productDependencyRepository.findAll().stream()
          .map(ProductDependency::getProductId).toList();
    }

    // Subtract existing product id
    List<String> missingProductIds = new ArrayList<>(availableProductIds);
    missingProductIds.removeAll(syncedProductIds);
    return missingProductIds;
  }

  private MavenArtifactModel findDownloadURLForDependency(String productId, String version, Dependency dependency) {
    String requestArtifactId = dependency.getArtifactId();

    MavenArtifactVersion mavenArtifactVersion = mavenArtifactVersionRepository.findById(productId).orElse(null);
    if (ObjectUtils.isEmpty(mavenArtifactVersion)) {
      return null;
    }

    MavenArtifactModel productArtifacts = filterMavenArtifactVersionByArtifactId(version, requestArtifactId,
        mavenArtifactVersion.getProductArtifactsByVersion());

    MavenArtifactModel additionalArtifact = filterMavenArtifactVersionByArtifactId(version, requestArtifactId,
        mavenArtifactVersion.getAdditionalArtifactsByVersion());

    return Optional.ofNullable(productArtifacts).orElse(additionalArtifact);
  }

  private MavenArtifactModel filterMavenArtifactVersionByArtifactId(String version, String compareArtifactId,
      List<MavenArtifactModel> artifactsByVersion) {
    return artifactsByVersion.stream()
        .filter(artifact -> artifact.getProductVersion().equals(version) &&
            artifact.getArtifactId().equals(compareArtifactId))
        .findAny()
        .orElse(null);
  }

  private List<Dependency> extractMavenPOMDependencies(MavenArtifactModel artifact) {
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

  private String downloadArtifactToLocal(MavenArtifactModel artifact) {
    var location = "";
    try {
      var unzipPath = String.join(File.separator, DATA_DIR, WORK_DIR, MAVEN_DIR, artifact.getArtifactId());
      location = fileDownloadService.downloadAndUnzipFile(artifact.getDownloadUrl(),
          new DownloadOption(true, unzipPath));
    } catch (Exception e) {
      log.error("Exception during unzip");
    }
    return location;
  }
}
