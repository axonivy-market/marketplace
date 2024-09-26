package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.MetadataSync;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.ProductJsonConstants.EN_LANGUAGE;

@Service
@Log4j2
public class MetadataServiceImpl implements MetadataService {
  private final ProductRepository productRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final ProductJsonContentRepository productJsonRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataRepository metadataRepo;
  private final ImageRepository imageRepository;
  private final ProductModuleContentRepository productModuleContentRepo;
  public static final String DEMO_SETUP_TITLE = "(?i)## Demo|## Setup";
  public static final String IMAGE_EXTENSION = "(.*?).(jpeg|jpg|png|gif)";
  public static final String README_IMAGE_FORMAT = "\\(([^)]*?%s[^)]*?)\\)";
  public static final String IMAGE_DOWNLOAD_URL_FORMAT = "(%s)";
  public static final String DESCRIPTION = "description";
  public static final String DEMO = "demo";
  public static final String SETUP = "setup";
  private static final String HASH = "#";


  public MetadataServiceImpl(ProductRepository productRepo, MetadataSyncRepository metadataSyncRepo,
      ProductJsonContentRepository productJsonRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo,
      MetadataRepository metadataRepo, ImageRepository imageRepository,
      ProductModuleContentRepository productModuleContentRepo) {
    this.productRepo = productRepo;
    this.metadataSyncRepo = metadataSyncRepo;
    this.productJsonRepo = productJsonRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.metadataRepo = metadataRepo;
    this.imageRepository = imageRepository;
    this.productModuleContentRepo = productModuleContentRepo;
  }

  private static void updateMavenArtifactVersionCacheWithModel(MavenArtifactVersion artifactVersionCache,
      String version, Metadata metadata) {
    if (metadata.isProductArtifact()) {
      if (artifactVersionCache.getProductArtifactsByVersion().computeIfAbsent(version,
          k -> new ArrayList<>()).stream().anyMatch(artifact -> metadata.getName().equals(artifact.getName()))) {
        return;
      }
      artifactVersionCache.getProductArtifactsByVersion().get(version).add(
          MavenUtils.buildMavenArtifactModelFromSnapShotMetadata(version, metadata));
    } else {
      artifactVersionCache.getAdditionalArtifactsByVersion().computeIfAbsent(version, k -> new ArrayList<>()).add(
          MavenUtils.buildMavenArtifactModelFromSnapShotMetadata(version, metadata));
    }
  }

  private void updateMavenArtifactVersionData(Product product, List<String> releasedVersions,
      Set<Metadata> metadataSet, MavenArtifactVersion artifactVersionCache) {
    for (Metadata metadata : metadataSet) {
      String metadataContent = MavenUtils.getMetadataContentFromUrl(metadata.getUrl());
      if (StringUtils.isBlank(metadataContent)) {
        continue;
      }
      MetadataReaderUtils.parseMetadataFromString(metadataContent, metadata);
      updateMavenArtifactVersionFromMetadata(artifactVersionCache, metadata);
      updateContentsFromNonMatchVersions(product.getId(), releasedVersions, metadata);
    }
  }

  public void syncAllProductMavenMetadata() {
    List<Product> products = productRepo.getAllProductWithIdAndReleaseTagAndArtifact();
    log.warn("**MetadataService: Start to sync version for {} product(s)", products.size());
    for (Product product : products) {
      // Set up cache before sync
      String productId = product.getId();
      Set<Metadata> metadataSet = new HashSet<>(metadataRepo.findByProductId(product.getId()));
      MavenArtifactVersion artifactVersionCache = mavenArtifactVersionRepo.findById(product.getId()).orElse(
          new MavenArtifactVersion(productId, new HashMap<>(), new HashMap<>()));
      MetadataSync syncCache = metadataSyncRepo.findById(product.getId()).orElse(
          MetadataSync.builder().productId(product.getId()).syncedTags(new HashSet<>()).build());
      Set<Artifact> artifactsFromNewTags = new HashSet<>();

      // Find artifacts form unhandled tags
      List<String> nonSyncedVersionOfTags = VersionUtils.getNonSyncedVersionOfTagsFromMetadataSync(
          product.getReleasedVersions(),
          syncCache);
      if (!CollectionUtils.isEmpty(nonSyncedVersionOfTags)) {
        updateArtifactsFromNonSyncedVersion(product.getId(), nonSyncedVersionOfTags, artifactsFromNewTags);
        log.info("**MetadataService: New tags detected: {} in product {}", nonSyncedVersionOfTags.toString(),
            productId);
      }

      // Sync versions from maven & update artifacts-version table
      List<Artifact> additionalArtifactFromMeta = product.getArtifacts();
      metadataSet.addAll(MavenUtils.convertArtifactsToMetadataSet(artifactsFromNewTags, productId));
      metadataSet.addAll(
          MavenUtils.convertArtifactsToMetadataSet(new HashSet<>(additionalArtifactFromMeta), productId));

      if (CollectionUtils.isEmpty(metadataSet)) {
        continue;
      }
      artifactVersionCache.setAdditionalArtifactsByVersion(new HashMap<>());
      updateMavenArtifactVersionData(product, product.getReleasedVersions(), metadataSet, artifactVersionCache);

      // Persist changed
      syncCache.getSyncedTags().addAll(nonSyncedVersionOfTags);
      metadataSyncRepo.save(syncCache);
      mavenArtifactVersionRepo.save(artifactVersionCache);
      metadataRepo.saveAll(metadataSet);
    }
    log.warn("**MetadataService: version sync finished");
  }

  private void updateContentsFromNonMatchVersions(String productId, List<String> releasedVersions,
      Metadata metadata) {
    Set<String> notInGHTags = new HashSet<>();
    List<ProductModuleContent> productModuleContents = new ArrayList<>();
    for (String metaVersion : metadata.getVersions()) {
      String matchedVersion = VersionUtils.getMavenVersionMatchWithTag(
          releasedVersions, metaVersion);
      if (matchedVersion == null && VersionUtils.isSnapshotVersion(metaVersion)) {
        notInGHTags.add(metaVersion);
      }
    }

    for (String notInGHTag : notInGHTags) {
      Metadata productArtifact = metadata.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX) ?
          metadata : null;
      if (Objects.nonNull(productArtifact)) {
        Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(productArtifact, notInGHTag);
        MetadataReaderUtils.parseMetadataSnapshotFromString(
            MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl()),
            snapShotMetadata);

        String url = MavenUtils.buildDownloadUrl(snapShotMetadata.getArtifactId(),
            notInGHTag, MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE,
            snapShotMetadata.getRepoUrl(), snapShotMetadata.getGroupId(), snapShotMetadata.getSnapshotVersionValue());

        if (StringUtils.isBlank(url)) {
          continue;
        }
        try {
          Product product = productRepo.findById(productId).orElse(null);
          if (Objects.isNull(product)) {
            continue;
          }
          ProductModuleContent productModuleContent = getReadmeAndProductContentsFromTag(product, notInGHTag,
              snapShotMetadata, url);
          if (productModuleContent != null) {
            productModuleContents.add(productModuleContent);
          }
        } catch (Exception e) {
          log.error("Cannot download and unzip file {}", e.getMessage());
        }
      }
    }
    if (!CollectionUtils.isEmpty(productModuleContents)) {
      productModuleContentRepo.saveAll(productModuleContents);
    }
  }

  public ProductModuleContent getReadmeAndProductContentsFromTag(Product product, String tag,
      Metadata snapShotMetadata, String url) {
    ProductModuleContent productModuleContent = new ProductModuleContent();
    try {
      String unzippedFolderPath = MetadataReaderUtils.downloadAndUnzipFile(url,
          snapShotMetadata);
      productModuleContent.setProductId(product.getId());
      productModuleContent.setTag(tag);
      productModuleContent.setRelatedMavenVersions(new HashSet<>());
      ProductFactory.mappingIdForProductModuleContent(productModuleContent);
      updateDependencyContentsFromProductJson(productModuleContent, product, unzippedFolderPath);
      extractReadMeFileFromContents(product, unzippedFolderPath, productModuleContent);
      Files.deleteIfExists(Path.of(unzippedFolderPath));
    } catch (Exception e) {
      log.error("Cannot get product.json content in {}", e.getMessage());
      return null;
    }
    return productModuleContent;
  }

  public void extractReadMeFileFromContents(Product product, String unzippedFolderPath,
      ProductModuleContent productModuleContent) {
    try {
      List<Path> readmeFiles = Files.walk(Paths.get(unzippedFolderPath))
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().startsWith(ReadmeConstants.README_FILE_NAME))
          .toList();

      Map<String, Map<String, String>> moduleContents = new HashMap<>();
      if (!readmeFiles.isEmpty()) {
        for (Path readmeFile : readmeFiles) {
          String readmeContents = Files.readString(readmeFile);
          if (hasImageDirectives(readmeContents)) {
            readmeContents = updateImagesWithDownloadUrl(product, unzippedFolderPath, readmeContents);
          }
          String locale = getReadmeFileLocale(readmeFile.getFileName().toString());
          getExtractedPartsOfReadme(moduleContents, readmeContents, locale);
        }
        productModuleContent.setDescription(replaceEmptyContentsWithEnContent(moduleContents.get(DESCRIPTION)));
        productModuleContent.setDemo(replaceEmptyContentsWithEnContent(moduleContents.get(DEMO)));
        productModuleContent.setSetup(replaceEmptyContentsWithEnContent(moduleContents.get(SETUP)));
      }
    } catch (Exception e) {
      log.error("Cannot get README file's content from folder {}: {}", unzippedFolderPath, e.getMessage());
    }
  }

  public Map<String, String> replaceEmptyContentsWithEnContent(Map<String, String> map) {
    String enValue = map.get(Language.EN.getValue());
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (StringUtils.isBlank(entry.getValue())) {
        map.put(entry.getKey(), enValue);
      }
    }
    return map;
  }

  private String updateImagesWithDownloadUrl(Product product, String unzippedFolderPath,
      String readmeContents) throws IOException {
    List<Path> imagesAtRootFolder = Files.walk(Paths.get(unzippedFolderPath))
        .filter(Files::isRegularFile)
        .filter(path -> path.getFileName().toString().toLowerCase().matches(IMAGE_EXTENSION))
        .toList();

    Map<String, String> imageUrls = new HashMap<>();
    for (Path imagePath : imagesAtRootFolder) {
      String imageName = imagePath.getFileName().toString();
      List<Image> existingImages = imageRepository.findByProductId(product.getId());

      InputStream contentStream = MavenUtils.extractedContentStream(imagePath);
      byte[] sourceBytes = IOUtils.toByteArray(contentStream);
      boolean isImageExisted =
          existingImages.stream().anyMatch(existingImage -> Arrays.equals(existingImage.getImageData().getData(),
              sourceBytes));
      if (isImageExisted) {
        continue;
      }

      Image image = new Image();
      image.setImageData(new Binary(sourceBytes));
      image.setProductId(product.getId());
      imageRepository.save(image);

      String imageUrl = CommonConstants.IMAGE_ID_PREFIX.concat(image.getId());
      imageUrls.put(imageName, imageUrl);
    }

    for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
      String imagePattern = String.format(README_IMAGE_FORMAT, Pattern.quote(entry.getKey()));
      readmeContents = readmeContents.replaceAll(imagePattern,
          String.format(IMAGE_DOWNLOAD_URL_FORMAT, entry.getValue()));
    }

    return readmeContents;
  }

  private String getReadmeFileLocale(String readmeFileName) {
    String result = StringUtils.EMPTY;
    Pattern pattern = Pattern.compile(GitHubConstants.README_FILE_LOCALE_REGEX);
    Matcher matcher = pattern.matcher(readmeFileName);
    if (matcher.find()) {
      result = matcher.group(1);
    }
    return result;
  }

  private boolean hasImageDirectives(String readmeContents) {
    Pattern pattern = Pattern.compile(IMAGE_EXTENSION);
    Matcher matcher = pattern.matcher(readmeContents);
    return matcher.find();
  }

  private void getExtractedPartsOfReadme(Map<String, Map<String, String>> moduleContents, String readmeContents,
      String locale) {
    String[] parts = readmeContents.split(DEMO_SETUP_TITLE);
    int demoIndex = readmeContents.indexOf(ReadmeConstants.DEMO_PART);
    int setupIndex = readmeContents.indexOf(ReadmeConstants.SETUP_PART);
    String description = Strings.EMPTY;
    String setup = Strings.EMPTY;
    String demo = Strings.EMPTY;

    if (parts.length > 0) {
      description = removeFirstLine(parts[0]);
    }

    if (demoIndex != -1 && setupIndex != -1) {
      if (demoIndex < setupIndex) {
        demo = parts[1];
        setup = parts[2];
      } else {
        setup = parts[1];
        demo = parts[2];
      }
    } else if (demoIndex != -1) {
      demo = parts[1];
    } else if (setupIndex != -1) {
      setup = parts[1];
    }

    locale = StringUtils.isEmpty(locale) ? Language.EN.getValue() : locale.toLowerCase();
    addLocaleContent(moduleContents, DESCRIPTION, description.trim(), locale);
    addLocaleContent(moduleContents, DEMO, demo.trim(), locale);
    addLocaleContent(moduleContents, SETUP, setup.trim(), locale);
  }

  private void addLocaleContent(Map<String, Map<String, String>> moduleContents, String type, String content,
      String locale) {
    moduleContents.computeIfAbsent(type, key -> new HashMap<>()).put(locale, content);
  }

  private String removeFirstLine(String text) {
    String result;
    if (text.isBlank()) {
      result = Strings.EMPTY;
    } else if (text.startsWith(HASH)) {
      int index = text.indexOf(StringUtils.LF);
      result = index != StringUtils.INDEX_NOT_FOUND ? text.substring(index + 1).trim() : Strings.EMPTY;
    } else {
      result = text;
    }

    return result;
  }

  private void updateDependencyContentsFromProductJson(ProductModuleContent productModuleContent,
      Product product, String unzippedFolderPath) throws IOException {
    List<Artifact> artifacts = MavenUtils.convertProductJsonToMavenProductInfo(
        Paths.get(unzippedFolderPath));

    Artifact artifact = artifacts.stream().filter(Artifact::getIsDependency).findFirst().orElse(null);
    if (Objects.nonNull(artifact)) {
      productModuleContent.setIsDependency(Boolean.TRUE);
      productModuleContent.setGroupId(artifact.getGroupId());
      productModuleContent.setArtifactId(artifact.getArtifactId());
      productModuleContent.setType(artifact.getType());
      productModuleContent.setName(artifact.getName());
    }

    String currentVersion = VersionUtils.convertTagToVersion(productModuleContent.getTag());
    Path productJsonPath = Paths.get(unzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE);
    String content = extractProductJsonContent(productJsonPath);

    if (ObjectUtils.isNotEmpty(content)) {
      ProductJsonContent jsonContent = new ProductJsonContent();
      jsonContent.setVersion(currentVersion);
      jsonContent.setProductId(product.getId());
      ProductFactory.mappingIdForProductJsonContent(jsonContent);
      jsonContent.setName(product.getNames().get(EN_LANGUAGE));
      jsonContent.setRelatedMavenVersions(new HashSet<>());
      jsonContent.setContent(content.replace(ProductJsonConstants.VERSION_VALUE, currentVersion));
      productJsonRepo.save(jsonContent);
    }
  }

  public String extractProductJsonContent(Path filePath) {
    try {
      InputStream contentStream = MavenUtils.extractedContentStream(filePath);
      return IOUtils.toString(contentStream, StandardCharsets.UTF_8);
    } catch (Exception exception) {
      log.error("Here");
      return null;
    }
  }

  public void updateMavenArtifactVersionFromMetadata(MavenArtifactVersion artifactVersionCache,
      Metadata metadata) {
    NonStandardProduct currentProduct = NonStandardProduct.findById(metadata.getProductId());
    metadata.getVersions().forEach(version -> {
      if (VersionUtils.isSnapshotVersion(version) && currentProduct != NonStandardProduct.PORTAL) {
        if (VersionUtils.isOfficialVersionOrUnReleasedDevVersion(metadata.getVersions().stream().toList(), version)) {
          updateMavenArtifactVersionForNonReleaseDeVersion(artifactVersionCache, metadata, version);
        }
      } else {
        updateMavenArtifactVersionCacheWithModel(artifactVersionCache, version, metadata);
      }
    });
  }

  public void updateMavenArtifactVersionForNonReleaseDeVersion(MavenArtifactVersion artifactVersionCache,
      Metadata metadata, String version) {
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(metadata, version);
    MetadataReaderUtils.parseMetadataSnapshotFromString(MavenUtils.getMetadataContentFromUrl(snapShotMetadata.getUrl()),
        snapShotMetadata);
    updateMavenArtifactVersionCacheWithModel(artifactVersionCache, version, snapShotMetadata);
  }

  public void updateArtifactsFromNonSyncedVersion(String productId, List<String> nonSyncedVersions,
      Set<Artifact> artifacts) {
    if (CollectionUtils.isEmpty(nonSyncedVersions)) {
      return;
    }
    nonSyncedVersions.forEach(version -> {
      ProductJsonContent productJson = productJsonRepo.findByProductIdAndVersion(productId, version);
      List<Artifact> artifactsInVersion = MavenUtils.getMavenArtifactsFromProductJson(productJson);
      artifacts.addAll(artifactsInVersion);
    });
  }
}