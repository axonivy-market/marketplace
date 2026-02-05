package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.comparator.LatestVersionComparator;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.core.utils.CoreMavenUtils;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.core.constants.CoreMavenConstants.TEST_ARTIFACT_ID;
import static com.axonivy.market.core.constants.CoreProductJsonConstants.NAME;

@Log4j2
@Service
@AllArgsConstructor
public class CoreVersionServiceImpl implements CoreVersionService {
  private final CoreProductJsonContentRepository coreProductJsonRepo;
  protected final CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepo;
  private final CoreMetadataRepository coreMetadataRepository;
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String productVersion) {
    Map<String, Object> result = new HashMap<>();
    if (StringUtils.isEmpty(productVersion)) {
      productVersion = getLatestReleasedVersion(productId);
    }
    var productJsonContent = coreProductJsonRepo.findByProductIdAndVersionIgnoreCase(productId,
        productVersion).stream().findAny().orElse(null);
    if (ObjectUtils.isEmpty(productJsonContent)) {
      return new HashMap<>();
    }
    try {
      result = mapper.readValue(productJsonContent.getContent(), Map.class);
      result.computeIfAbsent(NAME, k -> productJsonContent.getName());
    } catch (JsonProcessingException jsonProcessingException) {
      log.error(jsonProcessingException);
    }
    return result;
  }

  @Override
  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    List<MavenArtifactVersionModel> results = new ArrayList<>();
    List<MavenArtifactVersion> mavenArtifactVersions = coreMavenArtifactVersionRepo.findByProductId(productId);
    List<String> mavenVersions = getMavenVersionsToDisplay(mavenArtifactVersions, isShowDevVersion, designerVersion);
    for (String mavenVersion : mavenVersions) {
      List<MavenArtifactVersion> artifactsByVersion = filterArtifactByVersion(mavenArtifactVersions, mavenVersion);
      if (ObjectUtils.isNotEmpty(artifactsByVersion)) {
        results.add(new MavenArtifactVersionModel(mavenVersion, artifactsByVersion));
      }
    }
    return results;
  }

  @Override
  public List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions, Boolean isShowDevVersion,
      String designerVersion) {
    return CoreVersionUtils.extractAllVersions(mavenArtifactVersions, isShowDevVersion);
  }

  @Override
  public String getLatestReleasedVersion(String productId) {
    List<String> releasedVersions = getInstallableVersionsFromMetadataList(
        coreMetadataRepository.findByProductId(productId)).stream().dropWhile(
        version -> !CoreVersionUtils.isReleasedVersion(version)).toList();
    return CollectionUtils.firstElement(releasedVersions);
  }

  protected List<MavenArtifactVersion> filterArtifactByVersion(List<MavenArtifactVersion> mavenArtifactVersions,
      String mavenVersion) {
    return mavenArtifactVersions.stream().filter(
        artifact -> artifact.getId().getProductVersion().equals(mavenVersion)).distinct().sorted(
        Comparator.comparing((MavenArtifactVersion artifact) -> artifact.getId().getArtifactId()).thenComparing(
            artifact -> artifact.getId().getArtifactId().endsWith(TEST_ARTIFACT_ID))).toList();
  }

  public static List<String> getInstallableVersionsFromMetadataList(List<Metadata> metadataList) {
    List<String> installableVersions = new ArrayList<>();
    if (CollectionUtils.isEmpty(metadataList)) {
      return installableVersions;
    }
    metadataList.stream().filter(metadata -> CoreMavenUtils.isProductMetadata(metadata) && ObjectUtils.isNotEmpty(
        metadata.getVersions())).forEach(productMeta -> installableVersions.addAll(productMeta.getVersions()));
    return installableVersions.stream().distinct().sorted(new LatestVersionComparator()).toList();
  }
}
