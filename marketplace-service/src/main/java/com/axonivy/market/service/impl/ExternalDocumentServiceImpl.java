package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.config.MarketplaceConfig;
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
import java.util.Comparator;
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
        return null;
    }

    if (path.contains("index.html") || path.endsWith(".html")) {
        log.info("Path contains .html file, letting nginx handle it: {}", path);
        return null;
    }

    try {
        Path baseDir = Paths.get("/usr/share/nginx/html/market-cache/portal/portal-guide");

        String version = extractVersion(path);

        if (version == null) {
            String latestVersion = findLatestVersion(baseDir);
            return latestVersion != null
                    ? "/market-cache/portal/portal-guide/" + latestVersion + "/"
                    : null;
        }

        Path versionPath = baseDir.resolve(version);

        if (Files.isSymbolicLink(versionPath)) {
            Path targetPath = Files.readSymbolicLink(versionPath);
            Path realPath = baseDir.resolve(targetPath);

            if (hasIndex(realPath)) {
                return "/market-cache/portal/portal-guide/" + targetPath.getFileName() + "/";
            } else {
                return null; 
            }
        }

        if (Files.exists(versionPath) && hasIndex(versionPath)) {
            return "/market-cache/portal/portal-guide/" + version + "/";
        }

        String latestVersion = findLatestVersion(baseDir);
        if (latestVersion != null && !latestVersion.equals(version)) {
            return "/market-cache/portal/portal-guide/" + latestVersion + "/";
        }

        return null; 
    } catch (Exception e) {
        log.error("Error in resolveBestMatchRedirectUrl", e);
        return null;
    }
}

private boolean hasIndex(Path dir) {
    Path index = dir.resolve("doc/index.html");
    Path indexEn = dir.resolve("doc/en/index.html");
    return Files.exists(index) || Files.exists(indexEn);
}

private String findLatestVersion(Path baseDir) throws IOException {
    try (Stream<Path> paths = Files.list(baseDir)) {
        return paths
                .map(p -> p.getFileName().toString())
                .filter(v -> v.matches("\\d+(\\.\\d+)*(-m\\d+)?"))
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}

private String extractVersion(String path) {
    String[] segments = path.split("/");
    for (String seg : segments) {
        if (seg.matches("\\d+(\\.\\d+)*(-m\\d+)?") || seg.matches("dev")) {
            return seg;
        }
    }
    return null;
}
}