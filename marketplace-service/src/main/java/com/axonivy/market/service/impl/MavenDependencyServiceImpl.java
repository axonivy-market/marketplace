package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.bo.MavenDependency;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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

  private static Model convertPomToModel(File pomFile) throws XmlPullParserException {
    if (pomFile != null) {
      try (var inputStream = new FileInputStream(pomFile)) {
        return new MavenXpp3Reader().read(inputStream);
      } catch (IOException e) {
        log.error("Cannot read data from {} by {}", pomFile.toPath(), e.getMessage());
      }
    }
    return null;
  }

  @Override
  public int syncIARDependenciesForProducts(Boolean resetSync) {
    List<String> syncedProductIds = new ArrayList<>();
    List<String> missingProductIds = getMissingProductIds(resetSync);
    for (var productId : missingProductIds) {
      MavenArtifactVersion mavenArtifactVersion = mavenArtifactVersionRepository.findById(productId).orElse(null);
      // If no data in MavenArtifactVersion table then skip this product
      if (mavenArtifactVersion == null) {
        continue;
      }
      Map<String, List<MavenDependency>> dependenciesOfArtifact = new HashMap<>();
      var productDependency = ProductDependency.builder()
          .productId(productId)
          .dependenciesOfArtifact(dependenciesOfArtifact).build();

      // Base on version, loop the artifacts and maps its dependencies
      var productArtifactsByVersion = mavenArtifactVersion.getProductArtifactsByVersion();
      for (var version : productArtifactsByVersion.keySet()) {
        for (var artifact : productArtifactsByVersion.get(version)) {
          computeIARDependencies(productId, version, artifact, dependenciesOfArtifact);
        }
      }
      var additionalArtifactsByVersion = mavenArtifactVersion.getAdditionalArtifactsByVersion();
      for (var version : additionalArtifactsByVersion.keySet()) {
        for (var artifact : additionalArtifactsByVersion.get(version)) {
          computeIARDependencies(productId, version, artifact, dependenciesOfArtifact);
        }
      }

      dependenciesOfArtifact.entrySet().removeIf(entry -> entry.getValue().isEmpty());
      var savedItem = productDependencyRepository.save(productDependency);
      syncedProductIds.add(savedItem.getProductId());
    }
    return syncedProductIds.size();
  }

  private void computeIARDependencies(String productId, String version, MavenArtifactModel artifact,
                                      Map<String, List<MavenDependency>> dependenciesOfArtifact) {
    if (artifact == null || StringUtils.endsWith(artifact.getArtifactId(), DOC)
        || StringUtils.endsWith(artifact.getDownloadUrl(), DEFAULT_PRODUCT_FOLDER_TYPE)) {
      return;
    }
    var artifactId = artifact.getArtifactId();
    List<Dependency> dependencyModels = extractMavenDependencies(artifact);
    dependenciesOfArtifact.putIfAbsent(artifactId, new ArrayList<>());
    var artifactDependency = MavenDependency.builder()
        .version(version)
        .downloadUrl(artifact.getDownloadUrl())
        .dependencies(new ArrayList<>())
        .build();
    log.info("Collect IAR dependencies for requested artifact {}", artifactId);
    collectMavenDependenciesFor(productId, version, artifactDependency, dependencyModels);
    dependenciesOfArtifact.computeIfPresent(artifactId, (key, value) -> {
      value.add(artifactDependency);
      return value;
    });
  }

  private void collectMavenDependenciesFor(String productId, String version, MavenDependency mavenDependency, List<Dependency> dependencyModels) {
    if (ObjectUtils.isEmpty(dependencyModels)) {
      return;
    }

    for (var dependencyModel : dependencyModels) {
      var dependency = MavenDependency.builder().artifactId(dependencyModel.getArtifactId()).build();
      MavenArtifactModel dependencyArtifact = findDownloadURLForDependency(productId, version, dependencyModel);
      if (dependencyArtifact != null && StringUtils.isNotBlank(dependencyArtifact.getDownloadUrl())) {
        dependency.setDownloadUrl(dependencyArtifact.getDownloadUrl());
        mavenDependency.getDependencies().add(dependency);
        // Check does dependency artifact has IAR lib, e.g: portal
        log.info("Collect nested IAR dependencies for artifact {}", dependencyArtifact.getArtifactId());
        List<Dependency> dependenciesOfParent = extractMavenDependencies(dependencyArtifact);
        collectMavenDependenciesFor(productId, version, mavenDependency, dependenciesOfParent);
      }
    }
  }

  private List<String> getMissingProductIds(Boolean resetSync) {
    List<String> availableProductIds = productRepository.findAll().stream()
        .filter(product -> product.getListed() == null || Boolean.TRUE == product.getListed())
        .map(Product::getId).toList();
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
    List<MavenArtifactVersion> mavenArtifactVersions = mavenArtifactVersionRepository.findMavenArtifactVersions(
        productId, requestArtifactId, version.replace(DOT_SEPARATOR, UNDERSCORE));
    if (mavenArtifactVersions.isEmpty()) {
      return null;
    }

    for (var artifactVersion : mavenArtifactVersions) {
      var foundArtifact = filterMavenArtifactVersionByArtifactId(artifactVersion.getProductArtifactsByVersion(),
          version, requestArtifactId)
          .orElse(filterMavenArtifactVersionByArtifactId(artifactVersion.getAdditionalArtifactsByVersion(), version,
              requestArtifactId).orElse(null));
      if (foundArtifact != null) {
        return foundArtifact;
      }
    }
    return null;
  }

  private Optional<MavenArtifactModel> filterMavenArtifactVersionByArtifactId(
      Map<String, List<MavenArtifactModel>> artifactsByVersion,
      String version, String compareArtifactId) {
    if (ObjectUtils.isEmpty(artifactsByVersion)) {
      return Optional.empty();
    }
    return Optional.ofNullable(artifactsByVersion.get(version)).stream()
        .flatMap(Collection::stream)
        .filter(model -> model.getArtifactId().equals(compareArtifactId))
        .findAny();
  }

  private List<Dependency> extractMavenDependencies(MavenArtifactModel artifact) {
    List<Dependency> dependencies = new ArrayList<>();
    try {
      String location = fileDownloadService.downloadAndUnzipFile(artifact.getDownloadUrl(),
          new DownloadOption(true, String.join(File.separator, DATA_DIR, WORK_DIR, MAVEN_DIR, artifact.getArtifactId())));
      if (StringUtils.isBlank(location)) {
        return List.of();
      }

      File pomFile = new File(location + SLASH + POM);
      Model mavelModel = convertPomToModel(pomFile);
      dependencies = Optional.ofNullable(mavelModel).stream().map(Model::getDependencies).flatMap(Collection::stream)
          .filter(dependency -> ProductJsonConstants.DEFAULT_PRODUCT_TYPE.equals(dependency.getType()))
          .toList();
      // Delete work folder
      fileDownloadService.deleteDirectory(Path.of(location));
    } catch (Exception e) {
      log.error("Cannot extract maven dependencies {}", e.getMessage());
    }
    return dependencies;
  }
}
