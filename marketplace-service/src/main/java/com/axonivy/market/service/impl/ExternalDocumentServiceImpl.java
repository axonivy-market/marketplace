package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@AllArgsConstructor
@Service
public class ExternalDocumentServiceImpl implements ExternalDocumentService {

  private static final String DOC_URL_PATTERN = "/%s/index.html";
  private static final String MS_WIN_SEPARATOR = "\\\\";
  final ProductRepository productRepo;
  final ExternalDocumentMetaRepository externalDocumentMetaRepo;
  final FileDownloadService fileDownloadService;
  final ArtifactRepository artifactRepo;

  @Override
  public void syncDocumentForProduct(String productId, boolean isResetSync, String version) {
    var product = productRepo.findProductByIdAndRelatedData(productId);
    if (product == null) {
      log.warn("Cannot find the product for document sync {}", productId);
      return;
    }
    List<Artifact> docArtifacts = fetchDocArtifacts(product.getArtifacts());
    List<String> releasedVersions = getValidReleaseVersionsFromProduct(product.getReleasedVersions(), version);
    if (isNotEmpty(docArtifacts) && isNotEmpty(releasedVersions)) {
      downloadExternalDocumentFromMavenAndUpdateMetaData(productId, isResetSync, releasedVersions, docArtifacts);
    }
  }

  private static List<String> getValidReleaseVersionsFromProduct(List<String> releaseVersions, String version) {
    if (isEmpty(version)) {
      return Optional.ofNullable(releaseVersions).stream().flatMap(List::stream)
          .filter(VersionUtils::isValidFormatReleasedVersion).distinct().toList();
    }
    return List.of(version);
  }

  private void downloadExternalDocumentFromMavenAndUpdateMetaData(String productId, boolean isResetSync,
      List<String> releasedVersions, List<Artifact> docArtifacts) {
    if (isResetSync) {
      externalDocumentMetaRepo.deleteByProductIdAndVersionIn(productId, releasedVersions);
    }

    for (Artifact artifact : docArtifacts) {
      createDocumentationForProduct(productId, isResetSync, artifact, releasedVersions);
    }
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    return productRepo.findAllProductsHaveDocument();
  }

  @Override
  public ExternalDocumentMeta findExternalDocument(String productId, String version) {
    var product = productRepo.findById(productId);
    if (product.isEmpty()) {
      return null;
    }
    List<ExternalDocumentMeta> docMetas = externalDocumentMetaRepo.findByProductId(productId);
    List<String> docMetaVersion = docMetas.stream().map(ExternalDocumentMeta::getVersion).toList();
    String resolvedVersion = VersionFactory.get(docMetaVersion, version);
    return docMetas.stream().filter(meta -> StringUtils.equals(meta.getVersion(), resolvedVersion))
        .findAny().orElse(null);
  }

  private void createDocumentationForProduct(String productId, boolean isResetSync,
      Artifact artifact, List<String> releasedVersions) {
    List<String> missingVersions = getMissingVersions(productId, isResetSync, releasedVersions);
    for (String version : missingVersions) {
      var externalDocumentMeta = createDocumentMeta(productId, artifact, version, isResetSync);
      if (externalDocumentMeta != null) {
        externalDocumentMetaRepo.save(externalDocumentMeta);
      }
    }
  }

  private List<String> getMissingVersions(String productId, boolean isResetSync, List<String> releasedVersions) {
    List<String> missingVersions = new ArrayList<>(releasedVersions);
    if (!isResetSync) {
      List<String> existedDocMetaVersions = externalDocumentMetaRepo.findByProductIdAndVersionIn(productId,
          releasedVersions).stream().map(ExternalDocumentMeta::getVersion).toList();
      missingVersions = releasedVersions.stream().filter(version -> !existedDocMetaVersions.contains(version)).toList();
    }
    return missingVersions;
  }

  private ExternalDocumentMeta createDocumentMeta(String productId, Artifact artifact, String version,
      boolean isResetSync) {
    // Switch to nexus repo for artifact
    artifact.setRepoUrl(MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL);
    String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
    String location = downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
    if (StringUtils.isBlank(location)) {
      return null;
    }

    // remove prefix 'data' and replace all ms win separator to slash if present
    String locationRelative = location.substring(location.indexOf(DirectoryConstants.CACHE_DIR));
    locationRelative = RegExUtils.replaceAll(String.format(DOC_URL_PATTERN, locationRelative), MS_WIN_SEPARATOR,
        CommonConstants.SLASH);
    var externalDocumentMeta = new ExternalDocumentMeta();
    List<ExternalDocumentMeta> existingExternalDocumentMeta = externalDocumentMetaRepo.findByProductIdAndVersionIn(
        productId, List.of(version));
    if (!existingExternalDocumentMeta.isEmpty()) {
      externalDocumentMeta = existingExternalDocumentMeta.get(0);
    }
    externalDocumentMeta.setProductId(productId);
    externalDocumentMeta.setVersion(version);
    externalDocumentMeta.setArtifactId(artifact.getArtifactId());
    externalDocumentMeta.setArtifactName(artifact.getName());
    externalDocumentMeta.setRelativeLink(locationRelative);
    externalDocumentMeta.setStorageDirectory(location);

    return externalDocumentMeta;
  }

  private List<Artifact> fetchDocArtifacts(List<Artifact> artifacts) {
    List<String> artifactIds = artifacts.stream().map(Artifact::getId).distinct().toList();
    return artifactRepo.findAllByIdInAndFetchArchivedArtifacts(artifactIds).stream()
        .filter(artifact -> BooleanUtils.isTrue(artifact.getDoc()))
        .toList();
  }

  private String downloadDocAndUnzipToShareFolder(String downloadDocUrl, boolean isResetSync) {
    try {
      return fileDownloadService.downloadAndUnzipFile(downloadDocUrl,
          DownloadOption.builder().isForced(isResetSync).shouldGrantPermission(true).build());
    } catch (HttpClientErrorException e) {
      log.error("Cannot download doc {}", e.getStatusCode());
    } catch (Exception e) {
      log.error("Exception during unzip");
    }
    return EMPTY;
  }
}
