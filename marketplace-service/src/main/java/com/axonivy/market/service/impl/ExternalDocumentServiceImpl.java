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

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.axonivy.market.config.MarketplaceConfig;

@Log4j2
@RequiredArgsConstructor
@Service
public class ExternalDocumentServiceImpl implements ExternalDocumentService {

  private static final String DOC_URL_PATTERN = "/%s/index.html";
  private static final String MS_WIN_SEPARATOR = "\\\\";
  private final ProductRepository productRepo;
  private final ExternalDocumentMetaRepository externalDocumentMetaRepo;
  private final FileDownloadService fileDownloadService;
  private final ArtifactRepository artifactRepo;
  private final MarketplaceConfig marketplaceConfig;

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
      createExternalDocumentMetaForProduct(productId, isResetSync, artifact, releasedVersions);
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

  private void createExternalDocumentMetaForProduct(String productId, boolean isResetSync, Artifact artifact,
      List<String> releasedVersions) {
    List<String> missingVersions = getMissingVersions(productId, isResetSync, releasedVersions, artifact);
    log.warn("Missing ExternalDocumentMeta for {} with {} version(s)", productId, missingVersions);
    // Skip download doc to share folder on develop mode
    if (!shouldDownloadDocAndUnzipToShareFolder()) {
      log.warn("Create the ExternalDocumentMeta for the {} product was skipped due to " +
              "MARKET_ENVIRONMENT is not production - it was {}", productId, marketplaceConfig.getMarketEnvironment());
      return;
    }
    for (String version : missingVersions) {
      var externalDocumentMeta = createDocumentMeta(productId, artifact, version, isResetSync);
      if (externalDocumentMeta != null) {
        externalDocumentMetaRepo.save(externalDocumentMeta);
      }
    }
  }

  public boolean shouldDownloadDocAndUnzipToShareFolder() {
    return marketplaceConfig.isProduction();
  }

  private List<String> getMissingVersions(String productId, boolean isResetSync, List<String> releasedVersions,
      Artifact artifact) {
    List<String> missingVersions = new ArrayList<>(releasedVersions);
    if (!isResetSync) {
      List<String> existedDocMetaVersions = new ArrayList<>();
      // Find in DB by productId and versions, then double-check the share folder
      for (var docMeta : externalDocumentMetaRepo.findByProductIdAndVersionIn(productId, releasedVersions)) {
        String shareLocation = getShareFolderLocationByArtifactAndVersion(artifact, docMeta.getVersion());
        if (doesDocExistInShareFolder(shareLocation)) {
          existedDocMetaVersions.add(docMeta.getVersion());
        }
      }
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
    if (StringUtils.isBlank(location) || !doesDocExistInShareFolder(location)) {
      return null;
    }

    // remove prefix 'data' and replace all ms win separator to slash if present
    var relativeLocation = location.substring(location.indexOf(DirectoryConstants.CACHE_DIR));
    relativeLocation = RegExUtils.replaceAll(String.format(DOC_URL_PATTERN, relativeLocation), MS_WIN_SEPARATOR,
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
    externalDocumentMeta.setRelativeLink(relativeLocation);
    externalDocumentMeta.setStorageDirectory(location);

    return externalDocumentMeta;
  }

  public boolean doesDocExistInShareFolder(String location) {
    var shareFolder = new File(location);
    return shareFolder.exists() && ObjectUtils.isNotEmpty(shareFolder.listFiles());
  }

  private String getShareFolderLocationByArtifactAndVersion(Artifact artifact, String version) {
    String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
    return fileDownloadService.generateCacheStorageDirectory(downloadDocUrl);
  }

  private List<Artifact> fetchDocArtifacts(List<Artifact> artifacts) {
    List<String> artifactIds = artifacts.stream().map(Artifact::getId).distinct().toList();
    return artifactRepo.findAllByIdInAndFetchArchivedArtifacts(artifactIds).stream()
        .filter(artifact -> BooleanUtils.isTrue(artifact.getDoc()))
        .toList();
  }

  private String downloadDocAndUnzipToShareFolder(String downloadDocUrl, boolean isResetSync) {
    String workingDirectory = fileDownloadService.generateCacheStorageDirectory(downloadDocUrl);
    var downloadOption = DownloadOption.builder()
        .workingDirectory(workingDirectory)
        .isForced(isResetSync)
        .shouldGrantPermission(true)
        .build();
    try {
      return fileDownloadService.downloadAndUnzipFile(downloadDocUrl, downloadOption);
    } catch (HttpClientErrorException e) {
      log.error("Cannot download doc", e);
    } catch (IOException e) {
      log.error("Exception during unzip", e);
    }
    return EMPTY;
  }
}
