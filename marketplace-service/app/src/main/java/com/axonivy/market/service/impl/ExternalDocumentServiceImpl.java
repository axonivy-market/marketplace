package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.core.comparator.MavenVersionComparator;
import com.axonivy.market.config.MarketplaceConfig;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.DevelopmentVersion;
import com.axonivy.market.enums.DocumentLanguage;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.model.DocumentInfoResponse;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.rest.axonivy.AxonIvyClient;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.DocPathUtils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.util.DocPathUtils.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@RequiredArgsConstructor
@Service
public class ExternalDocumentServiceImpl implements ExternalDocumentService {

  private static final String DOC_URL_PATTERN = "/%s/index.html";
  private static final String MS_WIN_SEPARATOR = "\\\\";
  private static final Pattern SAFE_PATH_PATTERN = Pattern.compile(CommonConstants.SAFE_PATH_REGEX);
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
  public List<String> determineProductIdsForSync(String productId) {
    if (ObjectUtils.isNotEmpty(productId)) {
      return List.of(productId);
    }
    return findAllProductsHaveDocument().stream()
        .map(Product::getId)
        .toList();
  }

  @Override
  public void syncDocumentForProduct(String productId, boolean isResetSync, String forceSyncedVersion) {
    if (isRequestPathUnsafe(productId)) {
      log.warn("Rejected product ID path: {}", productId);
      return;
    }

    var product = productRepo.findProductByIdAndRelatedData(productId);
    if (product == null || isEmpty(product.getReleasedVersions())) {
      return;
    }

    var releasedVersions = product.getReleasedVersions();
    var specifiedVersion = EMPTY;
    if (StringUtils.isNotBlank(forceSyncedVersion)) {
      specifiedVersion = VersionUtils.normalizeVersion(forceSyncedVersion);
      if (!releasedVersions.contains(specifiedVersion)) {
        log.warn("The version {} has not been released yet.", specifiedVersion);
        return;
      }
    }

    var docArtifacts = fetchDocArtifacts(product.getArtifacts());
    if (!isEmpty(docArtifacts)) {
      downloadExternalDocumentFromMavenAndUpdateMetaData(productId, isResetSync, releasedVersions, docArtifacts,
          specifiedVersion);
    }
  }

  private void downloadExternalDocumentFromMavenAndUpdateMetaData(String productId, boolean isResetSync,
      List<String> releasedVersions, List<Artifact> docArtifacts, String forceSyncedVersion) {
    if (isResetSync) {
      deleteExternalDocumentMetaRepo(productId, releasedVersions, forceSyncedVersion);
    }

    // Skip download doc to share folder on develop mode
    if (!shouldDownloadDocAndUnzipToShareFolder()) {
      log.warn("Create the ExternalDocumentMeta for the {} product was skipped due to " +
          "MARKET_ENVIRONMENT is not production - it was {}", productId, marketplaceConfig.getMarketEnvironment());
      return;
    }

    Map<String, String> latestSupportedDocVersions = VersionFactory
        .getMapMajorVersionToLatestVersion(releasedVersions, majorVersions);
    log.warn("Latest supported doc versions for {}: {}", productId, latestSupportedDocVersions);

    for (Artifact artifact : docArtifacts) {
      List<String> needToBeSyncedDocVersions = StringUtils.isBlank(forceSyncedVersion) ?
          getMissingVersions(productId, isResetSync, releasedVersions, artifact) : List.of(forceSyncedVersion);
      needToBeSyncedDocVersions.forEach(version ->
          handleDocumentMeta(productId, artifact, version, isResetSync, latestSupportedDocVersions)
      );
    }
  }

  private void deleteExternalDocumentMetaRepo(String productId, List<String> versions,
      String forceDeletedVersion) {
    if (StringUtils.isNotBlank(forceDeletedVersion)) {
      externalDocumentMetaRepo.deleteByProductIdAndVersionIn(productId, List.of(forceDeletedVersion));
    } else {
      externalDocumentMetaRepo.deleteByProductIdAndVersionIn(productId,
          Stream.concat(versions.stream(), majorVersions.stream()).toList());
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
    String resolvedVersion = VersionFactory.get(docMetaVersion, version);
    return docMetas.stream().filter(meta -> StringUtils.equals(meta.getVersion(), resolvedVersion))
        .findAny().orElse(null);
  }

  @Override
  public DocumentInfoResponse findDocVersionsAndLanguages(String artifact, String version, String language,
      String host) {
    var selectedLanguage = DocumentLanguage.fromCode(language);
    List<ExternalDocumentMeta> docMetasByVersions =
        externalDocumentMetaRepo.findByArtifactNameAndVersionIn(artifact, majorVersions);

    if (docMetasByVersions.isEmpty()) {
      return null;
    }

    List<DocumentInfoResponse.DocumentVersion> documentVersions = docMetasByVersions.stream()
        .collect(Collectors.groupingBy(ExternalDocumentMeta::getVersion))
        .entrySet().stream()
        .sorted((Map.Entry<String,List<ExternalDocumentMeta>> v1,Map.Entry<String,List<ExternalDocumentMeta>> v2) -> {
          if (DevelopmentVersion.DEV.getCode().equalsIgnoreCase(v1.getKey())) {
            return 1;
          }
          if (DevelopmentVersion.DEV.getCode().equalsIgnoreCase(v2.getKey())) {
            return -1;
          }
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

    List<ExternalDocumentMeta> docMetasByLanguages =
        externalDocumentMetaRepo.findByArtifactNameAndVersionIn(artifact, Collections.singletonList(version));
    List<DocumentInfoResponse.DocumentLanguage> documentLanguages = docMetasByLanguages.stream()
        .map(docMeta -> DocumentInfoResponse.DocumentLanguage.builder()
            .language(docMeta.getLanguage().getCode())
            .url(host + docMeta.getRelativeLink())
            .build())
        .toList();

    return DocumentInfoResponse.builder().versions(documentVersions).languages(documentLanguages).build();
  }

  public String fallbackFindBestMatchVersion(String productId, String version) {
    var product = productRepo.findById(productId);
    if (product.isEmpty()) {
      return null;
    }
    List<ExternalDocumentMeta> docMetas = externalDocumentMetaRepo.findByProductId(productId);
    List<String> docMetaVersion = docMetas.stream().map(ExternalDocumentMeta::getVersion).toList();
    return VersionFactory.get(docMetaVersion, version);
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
      log.warn("Missing ExternalDocumentMeta for {} with {} version(s)", productId, missingVersions);
    }

    return missingVersions;
  }

  private void handleDocumentMeta(String productId, Artifact artifact, String version,
      boolean isResetSync, Map<String, String> latestSupportedDocVersions) {
    if (StringUtils.isBlank(version)) {
      return;
    }

    String location = getLocationByArtifactAndVersion(artifact, version, isResetSync);
    if (StringUtils.isBlank(location)) {
      return;
    }

    List<String> matchedMajorVersions = getMatchedMajorVersions(latestSupportedDocVersions, version);
    createSymlinkForMajorVersions(Paths.get(location), matchedMajorVersions);
    buildDocumentWithLanguage(location, artifact, productId, version, matchedMajorVersions);
  }

  private List<String> getMatchedMajorVersions(Map<String, String> latestSupportedDocVersions,
      String specifiedVersion) {
    if (CollectionUtils.isEmpty(latestSupportedDocVersions)) {
      return Collections.emptyList();
    }

    return latestSupportedDocVersions.entrySet()
        .stream()
        .filter(docVersion -> Objects.equals(docVersion.getValue(), specifiedVersion))
        .map(Map.Entry::getKey)
        .toList();
  }

  private String getLocationByArtifactAndVersion(Artifact artifact, String version, boolean isResetSync) {
    // Switch to nexus repo for artifact
    artifact.setRepoUrl(MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL);
    String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
    return downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
  }

  private void createSymlinkForMajorVersions(Path versionFolder, List<String> majorVersions) {
    if (versionFolder == null || CollectionUtils.isEmpty(majorVersions)) {
      return;
    }
    majorVersions.forEach(majorVersion -> createSymlinkForMajorVersion(versionFolder, majorVersion));
  }

  public String createSymlinkForMajorVersion(Path versionFolder, String majorVersion) {
    if (versionFolder == null || StringUtils.isBlank(majorVersion)) {
      return EMPTY;
    }

    return Optional.of(versionFolder)
        .map(Path::getParent)
        .map(parent -> createSymlinkForParent(parent, majorVersion))
        .orElse(EMPTY);
  }

  private String createSymlinkForParent(Path parent, String majorVersion) {
    if (isRequestPathUnsafe(majorVersion)) {
      return EMPTY;
    }
    var artifactRoot = parent.getParent();
    var fileName = parent.getFileName();
    if (artifactRoot == null || fileName == null || validatePathOutsideCacheRoot(artifactRoot)) {
      return null;
    }

    return createSymlinkSafely(artifactRoot, fileName, majorVersion);
  }

  private String createSymlinkSafely(Path artifactRoot, Path fileName, String majorVersion) {
    var symlinkPath = artifactRoot.resolve(majorVersion);

    try {
      prepareDirectoryForSymlinkPath(symlinkPath);
      var targetPath = Path.of(fileName.toString());
      Files.createSymbolicLink(symlinkPath, targetPath);
      return symlinkPath.toString();
    } catch (IOException e) {
      log.error("Cannot create symlink for major version {}: {}", majorVersion, e.getMessage());
      return null;
    }
  }

  void prepareDirectoryForSymlinkPath(Path symlinkPath) {
    if (!Files.exists(symlinkPath, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(symlinkPath)) {
      return;
    }

    if (Files.isDirectory(symlinkPath)) {
      fileDownloadService.deleteDirectory(symlinkPath);
    }
  }

  private void buildDocumentWithLanguage(String location, Artifact artifact, String productId, String version,
      List<String> majorVersions) {
    Map<DocumentLanguage, String> relativeLinkWithLanguage = getRelativePathWithLanguage(location);

    if (!relativeLinkWithLanguage.isEmpty()) {
      relativeLinkWithLanguage.forEach((DocumentLanguage language, String link) -> {
        buildExternalDocumentMetaWithLanguage(link, language, artifact, productId, version);
        if (!CollectionUtils.isEmpty(majorVersions)) {
          majorVersions.forEach((String majorVersion) -> {
            String majorLink = link.replace(version, majorVersion);
            buildExternalDocumentMetaWithLanguage(majorLink, language, artifact, productId, majorVersion);
          });
        }
      });
    } else {
      buildExternalDocumentMetaWithLanguage(location, DocumentLanguage.ENGLISH, artifact, productId, version);
      var docPath = Paths.get(location);
      var enPath = docPath.resolve(DocumentLanguage.ENGLISH.getCode());
      if (validatePathOutsideCacheRoot(enPath)) {
        return;
      }
      if (!Files.exists(enPath)) {
        createSymlinkForDocLanguage(enPath);
      }
    }
  }

  private void buildExternalDocumentMetaWithLanguage(String location, DocumentLanguage language,
      Artifact artifact, String productId, String version) {
    ExternalDocumentMeta meta = buildDocumentMeta(location, language, artifact, productId, version);
    externalDocumentMetaRepo.save(meta);
  }

  private void createSymlinkForDocLanguage(Path enPath) {
    try {
      var target = Path.of(CoreCommonConstants.DOT_SEPARATOR);
      prepareDirectoryForSymlinkPath(enPath);
      Files.createSymbolicLink(enPath, target);
    } catch (IOException e) {
      log.error("Cannot create symlink for doc/en: {}", e.getMessage());
    }
  }

  private boolean validatePathOutsideCacheRoot(Path path) {
    var cacheRoot = Paths.get(DirectoryConstants.DATA_CACHE_DIR).toAbsolutePath().normalize();
    var normalizedPath = path.toAbsolutePath().normalize();
    return !normalizedPath.startsWith(cacheRoot);
  }

  private ExternalDocumentMeta buildDocumentMeta(String location, DocumentLanguage language
      , Artifact artifact, String productId, String version) {
    // remove prefix 'data' and replace all ms win separator to slash if present
    var relativeLocation = location.substring(location.indexOf(DirectoryConstants.CACHE_DIR));
    relativeLocation = RegExUtils.replaceAll(String.format(DOC_URL_PATTERN, relativeLocation), MS_WIN_SEPARATOR,
        CoreCommonConstants.SLASH);
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
    return shareFolder.exists() && isNotEmpty(shareFolder.listFiles());
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
            name -> location + CoreCommonConstants.SLASH + name
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

  @Override
  public String resolveBestMatchRedirectUrl(String path) {
    String productName = extractProductId(path);
    String artifactName = extractArtifactName(path);
    String version = extractVersion(path);
    // If the path does not include artifactName, the artifactName will be created by productName
    // path = /portal/13.1.1/doc -> artifactName = portal-guide & version = 13.1.1
    if (VersionUtils.isMavenVersion(artifactName) || VersionUtils.isDevelopmentVersion(artifactName)) {
      version = artifactName;
      artifactName = createArtifactNameByProductName(productName);
    }

    if (StringUtils.isAnyBlank(productName, artifactName, version)) {
      return null;
    }

    DocumentLanguage language = ObjectUtils.defaultIfNull(extractLanguage(path), DocumentLanguage.ENGLISH);
    if (DevelopmentVersion.DEV.getCode().equalsIgnoreCase(
        version) || DevelopmentVersion.NIGHTLY.getCode().equalsIgnoreCase(version)) {
      return getRedirectURLForDevVersion(productName, artifactName, language);
    }

    return getRedirectURLForVersion(productName, artifactName, language, version);
  }

  private String getRedirectURLForDevVersion(String productName, String artifactName, DocumentLanguage language) {
    String devVersion = DevelopmentVersion.DEV.getCode();
    String redirectURL = DocPathUtils.generatePath(productName, artifactName, devVersion, language);
    if (!isSymlinkExisting(redirectURL)) {
      String bestMatchVersion = fallbackFindBestMatchVersion(productName, artifactName, devVersion);
      redirectURL = DocPathUtils.generatePath(productName, artifactName, bestMatchVersion, language);
      redirectURL = isSymlinkExisting(redirectURL) ? redirectURL : null;
    }
    return redirectURL;
  }

  private String getRedirectURLForVersion(String productName, String artifactName, DocumentLanguage language,
      String version) {
    String versionNumber = getVersionNumberFromDynamicDevelopmentVersions(version);
    String bestMatchVersion = resolveBestMatchSymlinkVersion(versionNumber);
    if (ObjectUtils.isEmpty(bestMatchVersion)) {
      bestMatchVersion = fallbackFindBestMatchVersion(productName, artifactName, versionNumber);
    }
    String redirectURL = DocPathUtils.generatePath(productName, artifactName, bestMatchVersion, language);
    return isSymlinkExisting(redirectURL) ? redirectURL : null;
  }

  private String getVersionNumberFromDynamicDevelopmentVersions(String version) {
    String result = version;
    for (DevelopmentVersion dv : DevelopmentVersion.DYNAMIC_DEVELOPMENT_VERSIONS) {
      result = getVersionNumberFromDevelopmentVersion(result, dv.getCode());
    }

    return result;
  }

  private String getVersionNumberFromDevelopmentVersion(String version, String developmentVersion) {
    if (StringUtils.isBlank(version)) {
      return EMPTY;
    }

    if (version.startsWith(developmentVersion) || version.endsWith(developmentVersion)) {
      return version.replace(developmentVersion, EMPTY).replace(CoreCommonConstants.DASH_SEPARATOR, EMPTY);
    }

    return version;
  }

  private String fallbackFindBestMatchVersion(String productName, String artifactName, String version) {
    String productId = getProductName(productName);
    String bestMatchVersion = fallbackFindBestMatchVersion(productId, version);
    if (StringUtils.isNoneBlank(productName, artifactName, bestMatchVersion)) {
      return bestMatchVersion;
    }
    return null;
  }

  public String resolveBestMatchSymlinkVersion(String version) {
    String bestMatchVersion = VersionFactory.getBestMatchMajorVersion(majorVersions, version);
    if (StringUtils.isBlank(bestMatchVersion)) {
      return EMPTY;
    }
    return bestMatchVersion;
  }

  private boolean isRequestPathUnsafe(String input) {
    if (StringUtils.isBlank(input) || !SAFE_PATH_PATTERN.matcher(input).matches()) {
      return true;
    }
    String[] forbiddenPathParts = {"..", CoreCommonConstants.SLASH, "\\", java.io.File.separator};
    return input.contains(String.join(EMPTY, forbiddenPathParts));
  }

  private boolean isSymlinkExisting(String symlinkPath) {
    try {
      String folderPath = DirectoryConstants.DATA_DIR + symlinkPath;
      return Files.exists(Paths.get(folderPath), LinkOption.NOFOLLOW_LINKS);
    } catch (InvalidPathException e) {
      log.error("Invalid path format for symlink check {}: {}", symlinkPath, e.getMessage());
    } catch (SecurityException e) {
      log.error("Security exception symlink existence for path {}: {}", symlinkPath, e.getMessage());
    }
    return false;
  }
}