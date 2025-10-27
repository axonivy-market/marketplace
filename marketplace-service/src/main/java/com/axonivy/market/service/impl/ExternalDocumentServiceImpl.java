package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.config.MarketplaceConfig;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.DevelopmentVersion;
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
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
      for (Artifact artifact : docArtifacts) {
        cleanUpSymlinksForArtifact(artifact, releasedVersions);
      }
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
    artifact.setRepoUrl(MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL);
    String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
    String location = downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
    if (StringUtils.isBlank(location)) {
      return;
    }
    if (latestSupportedDocVersions.containsKey(version)) {
      String majorVersion = latestSupportedDocVersions.get(version);
      String majorLocation = createSymlinkForMajorVersion(Paths.get(location), majorVersion, version);
      if (StringUtils.isNotBlank(majorLocation)) {
        buildDocumentWithLanguage(majorLocation, artifact, productId, majorVersion);
        createLanguageSymlinks(Paths.get(majorLocation));
      }
    }

    buildDocumentWithLanguage(location, artifact, productId, version);
    createLanguageSymlinks(Paths.get(location));
  }

  /**
   * Creates a symbolic link for major version pointing to the specific version folder.
   * 
   * @param versionFolder The path to the specific version folder (e.g., .../10.1.1/doc)
   * @param majorVersion The major version to create symlink for (e.g., "10")
   * @param specificVersion The specific version (e.g., "10.1.1")
   * @return The path to the symlinked major version folder, or empty string on failure
   */
  private String createSymlinkForMajorVersion(Path versionFolder, String majorVersion, String specificVersion) {
    try {
      Path specificVersionParent = versionFolder.getParent();
      Path artifactRoot = specificVersionParent.getParent();
      Path majorVersionPath = artifactRoot.resolve(majorVersion);
      
      if (Files.exists(majorVersionPath)) {
        if (Files.isSymbolicLink(majorVersionPath)) {
          Files.delete(majorVersionPath);
          log.info("Deleted existing symlink: {}", majorVersionPath);
        } else {
          try {
            org.apache.commons.io.FileUtils.deleteDirectory(majorVersionPath.toFile());
            log.info("Force deleted existing directory to create symlink: {}", majorVersionPath);
          } catch (IOException deleteEx) {
            log.warn("Failed to delete existing directory: {}. Skipping symlink creation.", majorVersionPath, deleteEx);
            return EMPTY;
          }
        }
      }
      
      Path relativeTarget = Path.of(specificVersionParent.getFileName().toString());
      Files.createSymbolicLink(majorVersionPath, relativeTarget);
      log.info("Created symlink: {} -> {}", majorVersionPath, relativeTarget);
      
      return majorVersionPath + File.separator + DOC_DIR;
      
    } catch (IOException e) {
      log.error("Failed to create symlink for major version {} pointing to {}", majorVersion, specificVersion, e);
      return EMPTY;
    }
  }

  /**
   * Create language symlinks in the doc directory
   * Creates an 'en' symlink that points to the root doc directory as fallback for English
   * 
   * @param docPath The path to the doc directory (e.g., /market-cache/portal/portal-guide/13.1/doc)
   */
  private void createLanguageSymlinks(Path docPath) {
    try {
      if (!Files.exists(docPath) || !Files.isDirectory(docPath)) {
        return;
      }

      Path enSymlink = docPath.resolve("en");
      
      if (Files.exists(enSymlink)) {
        if (Files.isSymbolicLink(enSymlink)) {
          Files.delete(enSymlink);
          log.info("Deleted existing language symlink: {}", enSymlink);
        } else if (Files.isDirectory(enSymlink)) {
          log.info("Directory 'en' already exists, skipping symlink creation: {}", enSymlink);
          return;
        }
      }
      
      Path relativeTarget = Path.of(".");
      Files.createSymbolicLink(enSymlink, relativeTarget);
      log.info("Created language symlink: {} -> {}", enSymlink, relativeTarget);
      
    } catch (IOException e) {
      log.error("Failed to create language symlinks for path {}", docPath, e);
    }
  }

  /**
   * Clean up symlinks for an artifact when resetting sync
   */
  private void cleanUpSymlinksForArtifact(Artifact artifact, List<String> releasedVersions) {
    try {
      String sampleVersion = releasedVersions.isEmpty() ? "1.0.0" : releasedVersions.get(0);
      String sampleLocation = getShareFolderLocationByArtifactAndVersion(artifact, sampleVersion);
      Path artifactRoot = Paths.get(sampleLocation).getParent().getParent();
      
      if (!Files.exists(artifactRoot)) {
        return;
      }
      
      Files.list(artifactRoot)
          .filter(Files::isSymbolicLink)
          .forEach(symlink -> {
            try {
              Files.delete(symlink);
            } catch (IOException e) {
              log.error("Failed to delete symlink: {}", symlink, e);
            }
          });
      
    } catch (IOException e) {
      log.error("Error during symlink cleanup for artifact {}", artifact.getArtifactId(), e);
    }
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

  @Override
public String resolveBestMatchRedirectUrl(String path) {
  if (path == null || path.trim().isEmpty()) {
    log.warn("#resolveBestMatchRedirectUrl The Path is null or empty.");
    return null;
  }

  String normalizedPath = path.trim().replaceAll("^/+|/+$", "");
  log.info("#resolveBestMatchRedirectUrl Processing path: {}", normalizedPath);

  String[] segments = normalizedPath.split("/");
  if (segments.length < 2) {
    log.warn("#resolveBestMatchRedirectUrl Unable to process path {}", path);
    return null;
  }

  String productCategory = segments[0];
  String productName = segments[1];

  try {
    String version = null;
    String language = null;

    if (segments.length == 5 && DOC_DIR.equals(segments[3])) {
      version = segments[2];
      language = segments[4];
      return buildDocRedirectUrl(productCategory, productName, version, language);
    }

    if (segments.length == 3 || (segments.length == 4 && DOC_DIR.equals(segments[3]))) {
      version = segments[2];
      return buildDocRedirectUrl(productCategory, productName, version, null);
    }

    if (segments.length == 2) {
      String latestVersion = switch (productCategory) {
        case "portal", "docfactory" -> "13.1";
        default -> "dev";
      };
      return buildDocRedirectUrl(productCategory, productName, latestVersion, null);
    }

    if (segments.length > 5) {
      String extractedVersion = DocPathUtils.extractVersion(path);
      String extractedProductId = DocPathUtils.extractProductId(path);

      if (extractedProductId != null && extractedVersion != null) {
        String bestMatchVersion = findBestMatchVersion(extractedProductId, extractedVersion);
        if (bestMatchVersion != null) {
          String updatedPath = DocPathUtils.updateVersionInPath(path, bestMatchVersion, extractedVersion);
          var resolvedPath = DocPathUtils.resolveDocPath(updatedPath);

          if (resolvedPath != null && Files.exists(resolvedPath)) {
            String redirectUrl = CommonConstants.SLASH + DirectoryConstants.CACHE_DIR + updatedPath;
            log.info("#resolveBestMatchRedirectUrl Redirecting full path to: {}", redirectUrl);
            return redirectUrl;
          }
        }
      }
      return null;
    }

  } catch (Exception e) {
    log.error("#resolveBestMatchRedirectUrl Error processing path {}: {}", path, e.getMessage());
  }

  log.warn("#resolveBestMatchRedirectUrl Unable to process path {}", path);
  return null;
}

/**
 * Build redirect URL with fallback logic and ensure trailing slash:
 *  - Always redirect to directory with trailing slash
 *  - If language exists → use it
 *  - Else → try 'en'
 *  - Else → fallback to doc root
 */
private String buildDocRedirectUrl(String productCategory, String productName, String version, String language) {
  String baseUrl = CommonConstants.SLASH + DirectoryConstants.CACHE_DIR
      + CommonConstants.SLASH + productCategory
      + CommonConstants.SLASH + productName
      + CommonConstants.SLASH + version
      + CommonConstants.SLASH + DOC_DIR;

  if (language != null) {
    Path langPath = Paths.get(DirectoryConstants.CACHE_DIR, productCategory, productName, version, DOC_DIR, language);
    if (Files.exists(langPath)) {
      String redirectUrl = baseUrl + CommonConstants.SLASH + language + CommonConstants.SLASH;
      log.info("#resolveBestMatchRedirectUrl Redirecting to: {}", redirectUrl);
      return redirectUrl;
    } else {
      log.warn("#resolveBestMatchRedirectUrl Language {} not found, fallback to English/doc root", language);
    }
  }

  Path enPath = Paths.get(DirectoryConstants.CACHE_DIR, productCategory, productName, version, DOC_DIR, "en");
  if (Files.exists(enPath)) {
    String redirectUrl = baseUrl + CommonConstants.SLASH + "en" + CommonConstants.SLASH;
    log.info("#resolveBestMatchRedirectUrl Redirecting to English version: {}", redirectUrl);
    return redirectUrl;
  }

  String redirectUrl = baseUrl + CommonConstants.SLASH;
  log.info("#resolveBestMatchRedirectUrl Fallback to doc root: {}", redirectUrl);
  return redirectUrl;
}
}