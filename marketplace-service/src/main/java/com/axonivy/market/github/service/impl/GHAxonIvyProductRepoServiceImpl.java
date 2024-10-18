package com.axonivy.market.github.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.util.ProductContentUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.constants.CommonConstants.IMAGE_ID_PREFIX;
import static com.axonivy.market.constants.GitHubConstants.MG_GRAPH_IMAGES_FOR_SETUP_FILE;
import static com.axonivy.market.constants.GitHubConstants.MS_GRAPH_PRODUCT_DIRECTORY;
import static com.axonivy.market.constants.ReadmeConstants.SETUP_FILE;
import static com.axonivy.market.util.ProductContentUtils.SETUP;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
  private final GitHubService gitHubService;
  private final ImageService imageService;
  private GHOrganization organization;
  private final ProductJsonContentService productJsonContentService;

  public GHAxonIvyProductRepoServiceImpl(GitHubService gitHubService, ImageService imageService,
      ProductJsonContentService productJsonContentService) {
    this.gitHubService = gitHubService;
    this.imageService = imageService;
    this.productJsonContentService = productJsonContentService;
  }

  private static GHContent getProductJsonFile(List<GHContent> contents) {
    return contents.stream().filter(GHContent::isFile)
        .filter(content -> ProductJsonConstants.PRODUCT_JSON_FILE.equals(content.getName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion) {
    try {
      return getOrganization().getRepository(repoName).getFileContent(filePath, tagVersion);
    } catch (IOException e) {
      log.error("Cannot Get Content From File Directory", e);
      return null;
    }
  }

  public GHOrganization getOrganization() throws IOException {
    if (organization == null) {
      organization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    }
    return organization;
  }

  @Override
  public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
    return getOrganization().getRepository(repoName).listTags().toList();
  }

  @Override
  public ProductModuleContent getReadmeAndProductContentsFromTag(Product product, GHRepository ghRepository,
      String tag) {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(product.getId(), tag,
        new HashSet<>());
    try {
      List<GHContent> contents = getProductFolderContents(product.getId(), ghRepository, tag);
      updateDependencyContentsFromProductJson(productModuleContent, contents, product);
      extractReadMeFileFromContents(product, contents, productModuleContent);
    } catch (Exception e) {
      log.error("Cannot get product.json content in {} - {}", ghRepository.getName(), e.getMessage());
      return null;
    }
    return productModuleContent;
  }

  public void extractReadMeFileFromContents(Product product, List<GHContent> contents,
      ProductModuleContent productModuleContent) {
    try {
      List<GHContent> readmeFiles = contents.stream().filter(GHContent::isFile)
          .filter(content -> content.getName().startsWith(ReadmeConstants.README_FILE_NAME)).toList();
      Map<String, Map<String, String>> moduleContents = new HashMap<>();

      if (!CollectionUtils.isEmpty(readmeFiles)) {
        for (GHContent readmeFile : readmeFiles) {
          String readmeContents = new String(readmeFile.read().readAllBytes());
          if (ProductContentUtils.hasImageDirectives(readmeContents)) {
            readmeContents = updateImagesWithDownloadUrl(product.getId(), contents, readmeContents);
          }
          ProductContentUtils.getExtractedPartsOfReadme(moduleContents, readmeContents, readmeFile.getName());
          updateSetupPartForProductModuleContent(product, moduleContents, productModuleContent.getTag());
        }
        ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
      }
    } catch (Exception e) {
      log.error("Cannot get README file's content {}", e.getMessage());
    }
  }

  @Override
  public void updateSetupPartForProductModuleContent(Product product,
      Map<String, Map<String, String>> moduleContents, String tag) throws IOException {
    if (!NonStandardProduct.isMsGraphProduct(product.getId())) {
      return;
    }

    GHRepository ghRepository = gitHubService.getRepository(product.getRepositoryName());
    List<GHContent> contents = ghRepository.getDirectoryContent(MS_GRAPH_PRODUCT_DIRECTORY, tag);

    GHContent setupFile = contents.stream().filter(GHContent::isFile)
        .filter(content -> content.getName().equalsIgnoreCase(SETUP_FILE))
        .findFirst().orElse(null);

    if (ObjectUtils.isNotEmpty(setupFile)) {
      String setupContent = new String(setupFile.read().readAllBytes());
      if (ProductContentUtils.hasImageDirectives(setupContent)) {
        List<GHContent> setupImagesFolder =
            contents.stream().filter(content -> content.getName().equals(MG_GRAPH_IMAGES_FOR_SETUP_FILE)).toList();
        setupContent = updateImagesWithDownloadUrl(product.getId(), setupImagesFolder, setupContent);
      }

      if (setupContent.contains(ReadmeConstants.SETUP_PART)) {
        List<String> extractSetupContent = List.of(setupContent.split(ReadmeConstants.SETUP_PART));
        setupContent = ProductContentUtils.removeFirstLine(extractSetupContent.get(1));
      }
      ProductContentUtils.addLocaleContent(moduleContents, SETUP, setupContent, Language.EN.getValue());
    }
  }

  private void updateDependencyContentsFromProductJson(ProductModuleContent productModuleContent,
      List<GHContent> contents, Product product) throws IOException {
    GHContent productJsonFile = getProductJsonFile(contents);
    if (Objects.nonNull(productJsonFile)) {
      List<Artifact> artifacts = GitHubUtils.convertProductJsonToMavenProductInfo(productJsonFile);
      ProductContentUtils.updateProductModule(productModuleContent, artifacts);
      String currentVersion = VersionUtils.convertTagToVersion(productModuleContent.getTag());
      String content = extractProductJsonContent(productJsonFile, productModuleContent.getTag());
      productJsonContentService.updateProductJsonContent(content, productModuleContent.getTag(), currentVersion,
          ProductJsonConstants.VERSION_VALUE, product);
    }
  }

  public String extractProductJsonContent(GHContent ghContent, String tag) {
    try {
      InputStream contentStream = GitHubUtils.extractedContentStream(ghContent);
      return IOUtils.toString(contentStream, StandardCharsets.UTF_8);
    } catch (Exception exception) {
      log.error("Cannot paste content of product.json {} at tag: {}", ghContent.getPath(), tag);
      return null;
    }
  }

  public String updateImagesWithDownloadUrl(String productId, List<GHContent> contents, String readmeContents) {
    List<GHContent> allContentOfImages = getAllImagesFromProductFolder(contents);
    Map<String, String> imageUrls = new HashMap<>();

    allContentOfImages.forEach(content -> Optional.of(imageService.mappingImageFromGHContent(productId, content, false))
        .ifPresent(image -> imageUrls.put(content.getName(), IMAGE_ID_PREFIX.concat(image.getId()))));
    return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
  }

  private List<GHContent> getAllImagesFromProductFolder(List<GHContent> productFolderContents) {
    List<GHContent> images = new ArrayList<>();
    GitHubUtils.findImages(productFolderContents, images);
    return images;
  }

  private List<GHContent> getProductFolderContents(String productId, GHRepository ghRepository, String tag)
      throws IOException {
    String productFolderPath = ghRepository.getDirectoryContent(CommonConstants.SLASH, tag).stream()
        .filter(GHContent::isDirectory).map(GHContent::getName)
        .filter(content -> content.endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findFirst().orElse(null);
    productFolderPath = NonStandardProduct.findById(productId, productFolderPath);

    return ghRepository.getDirectoryContent(productFolderPath, tag);
  }
}
