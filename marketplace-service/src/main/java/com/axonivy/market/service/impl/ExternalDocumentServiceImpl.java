package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.config.MarketplaceConfig;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.DocumentLanguage;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.model.DocumentInfoResponse;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.rest.axonivy.AxonIvyClient;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.DirectoryConstants.DOC_DIR;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@RequiredArgsConstructor
@Service
public class ExternalDocumentServiceImpl implements ExternalDocumentService {

  private static final String DOC_URL_PATTERN = "/%s/index.html";
  private static final String MS_WIN_SEPARATOR = "\\\\";
  private static final String DEV = "dev";
  private final ProductRepository productRepo;
  private final ExternalDocumentMetaRepository externalDocumentMetaRepo;
  private final FileDownloadService fileDownloadService;
  private final ArtifactRepository artifactRepo;
  private final MarketplaceConfig marketplaceConfig;
  private final AxonIvyClient axonIvyClient;
  private List<String> majorVersions;

  @PostConstruct
  public void init() {
    majorVersions = axonIvyClient.getDocumentVersions();
  }

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
      externalDocumentMetaRepo.deleteByProductIdAndVersionIn(productId,
          Stream.concat(releasedVersions.stream(), majorVersions.stream()).toList());
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
    List<ExternalDocumentMeta> docMetas = externalDocumentMetaRepo
        .findByProductIdAndLanguage(productId, DocumentLanguage.ENGLISH);
    List<String> docMetaVersion = docMetas.stream().map(ExternalDocumentMeta::getVersion).toList();
    String resolvedVersion = VersionFactory.getBestMatchMajorVersion(docMetaVersion, version, majorVersions);
    return docMetas.stream().filter(meta -> StringUtils.equals(meta.getVersion(), resolvedVersion))
        .findAny().orElse(null);
  }

  @Override
  public DocumentInfoResponse findDocVersionsAndLanguages(String artifact, String version, String language,
      String host) {
    var selectedLanguage = DocumentLanguage.fromCode(language);
    List<ExternalDocumentMeta> docMetasByVersions =
        externalDocumentMetaRepo.findByArtifactNameAndVersionIn(artifact, majorVersions);

    List<ExternalDocumentMeta> docMetasByLanguages =
        externalDocumentMetaRepo.findByArtifactNameAndVersionIn(artifact, Collections.singletonList(version));

    if (docMetasByVersions.isEmpty()) {
      return null;
    }

    List<DocumentInfoResponse.DocumentVersion> documentVersions = docMetasByVersions.stream()
        .collect(Collectors.groupingBy(ExternalDocumentMeta::getVersion))
        .entrySet().stream()
        .sorted((v1, v2) -> {
          if (DEV.equalsIgnoreCase(v1.getKey())) return 1;
          if (DEV.equalsIgnoreCase(v2.getKey())) return -1;
          return MavenVersionComparator.compare(v1.getKey(), v2.getKey());
        })
        .map((Map.Entry<String, List<ExternalDocumentMeta>> entry) -> {
          String ver = entry.getKey();
          ExternalDocumentMeta chosenMeta = entry.getValue().stream()
              .filter(meta -> meta.getLanguage() != null && meta.getLanguage().equals(selectedLanguage))
              .findFirst()
              .orElse(entry.getValue().get(0));
          return DocumentInfoResponse.DocumentVersion.builder().version(ver)
              .url(host + chosenMeta.getRelativeLink()).build();
        }).toList();

    List<DocumentInfoResponse.DocumentLanguage> documentLanguages = docMetasByLanguages.stream()
        .map(docMeta -> DocumentInfoResponse.DocumentLanguage.builder()
            .language(docMeta.getLanguage().getCode())
            .url(host + docMeta.getRelativeLink())
            .build())
        .toList();

    return DocumentInfoResponse.builder().versions(documentVersions).languages(documentLanguages).build();
  }

  public String findBestMatchVersion(String productId, String version) {
    var product = productRepo.findById(productId);
    if (product.isEmpty()) {
      return null;
    }
    List<ExternalDocumentMeta> docMetas = externalDocumentMetaRepo.findByProductId(productId);
    List<String> docMetaVersion = docMetas.stream().map(ExternalDocumentMeta::getVersion).toList();
    return VersionFactory.getBestMatchMajorVersion(docMetaVersion, version, majorVersions);
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
    Map<String, String> latestSupportedDocVersions = VersionFactory
        .getMapMajorVersionToLatestVersion(releasedVersions, majorVersions);

    log.warn("Latest supported doc versions for {}: {}", productId, latestSupportedDocVersions);
    for (String version : missingVersions) {
      handleDocumentMeta(productId, artifact, version, isResetSync, latestSupportedDocVersions);
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

  private void handleDocumentMeta(String productId, Artifact artifact, String version,
      boolean isResetSync, Map<String, String> latestSupportedDocVersions) {
    // Switch to nexus repo for artifact
    artifact.setRepoUrl(MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL);
    String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
    String location = downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
    if (StringUtils.isBlank(location)) {
      return;
    }
    if (latestSupportedDocVersions.containsKey(version)) {
      String majorVersion = latestSupportedDocVersions.get(version);
      String majorLocation = updateLatestFolder(Paths.get(location), majorVersion);
      buildDocumentWithLanguage(majorLocation, artifact, productId, majorVersion);
    }
    buildDocumentWithLanguage(location, artifact, productId, version);
  }

  private void buildDocumentWithLanguage(String location, Artifact artifact, String productId, String version) {
    Map<DocumentLanguage, String> relativeLinkWithLanguage = getRelativePathWithLanguage(location);

    if (!relativeLinkWithLanguage.isEmpty()) {
      relativeLinkWithLanguage.forEach((DocumentLanguage language, String link) -> {
        ExternalDocumentMeta meta = buildDocumentMeta(link, language, artifact, productId, version);
        externalDocumentMetaRepo.save(meta);
      });
    } else {
      ExternalDocumentMeta meta = buildDocumentMeta(location, DocumentLanguage.ENGLISH, artifact, productId, version);
      externalDocumentMetaRepo.save(meta);
    }
  }

  private ExternalDocumentMeta buildDocumentMeta(String location, DocumentLanguage language
      , Artifact artifact, String productId, String version) {
    // remove prefix 'data' and replace all ms win separator to slash if present
    var relativeLocation = location.substring(location.indexOf(DirectoryConstants.CACHE_DIR));
    relativeLocation = RegExUtils.replaceAll(String.format(DOC_URL_PATTERN, relativeLocation), MS_WIN_SEPARATOR,
        SLASH);
    var externalDocumentMeta = new ExternalDocumentMeta();
    List<ExternalDocumentMeta> existingExternalDocumentMeta = externalDocumentMetaRepo
        .findByProductIdAndLanguageAndVersion(productId, language, version);
    if (!existingExternalDocumentMeta.isEmpty()) {
      externalDocumentMeta = existingExternalDocumentMeta.get(0);
    }
    externalDocumentMeta.setProductId(productId);
    externalDocumentMeta.setVersion(version);
    externalDocumentMeta.setArtifactId(artifact.getArtifactId());
    externalDocumentMeta.setArtifactName(artifact.getName());
    externalDocumentMeta.setRelativeLink(relativeLocation);
    externalDocumentMeta.setStorageDirectory(location);
    externalDocumentMeta.setLanguage(language);

    return externalDocumentMeta;
  }

  public boolean doesDocExistInShareFolder(String location) {
    var shareFolder = new File(location);
    return shareFolder.exists() && ObjectUtils.isNotEmpty(shareFolder.listFiles());
  }

  public Map<DocumentLanguage, String> getRelativePathWithLanguage(String location) {
    var shareFolder = new File(location);
    if (!shareFolder.isDirectory()) {
      return Map.of();
    }

    File[] files = shareFolder.listFiles(File::isDirectory);
    if (files == null) {
      return Map.of();
    }

    return Arrays.stream(files)
        .map(File::getName)
        .filter(name -> DocumentLanguage.getCodes().contains(name))
        .collect(Collectors.toMap(
            DocumentLanguage::fromCode,
            name -> location + SLASH + name
        ));
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

  public String updateLatestFolder(Path versionFolder, String majorVersion) {
    Path parent = versionFolder.getParent().getParent();
    Path majorFolder = parent.resolve(majorVersion);
    FileUtils.duplicateFolder(versionFolder.getParent(), majorFolder);
    return majorFolder + File.separator + DOC_DIR;
  }
}
