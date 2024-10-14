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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(product, tag,
        new HashSet<>());
    try {
      List<GHContent> contents = getProductFolderContents(product, ghRepository, tag);
      updateDependencyContentsFromProductJson(productModuleContent, contents, product);
      extractReadMeFileFromContents(product, contents, productModuleContent, tag);
    } catch (Exception e) {
      log.error("Cannot get product.json content in {} - {}", ghRepository.getName(), e.getMessage());
      return null;
    }
    return productModuleContent;
  }

  public void extractReadMeFileFromContents(Product product, List<GHContent> contents,
      ProductModuleContent productModuleContent, String tag) {
    try {
      List<GHContent> readmeFiles = contents.stream().filter(GHContent::isFile)
          .filter(content -> content.getName().startsWith(ReadmeConstants.README_FILE_NAME)).toList();
      Map<String, Map<String, String>> moduleContents = new HashMap<>();

      if (!CollectionUtils.isEmpty(readmeFiles)) {
        for (GHContent readmeFile : readmeFiles) {
          String readmeContents = new String(readmeFile.read().readAllBytes());
          if (readmeContents.contains(ReadmeConstants.VARIABLE_DIR)) {
            readmeContents = replaceVariable(readmeContents, product, tag);
          }

          if (ProductContentUtils.hasImageDirectives(readmeContents)) {
            readmeContents = updateImagesWithDownloadUrl(product, contents, readmeContents);
          }
          ProductContentUtils.getExtractedPartsOfReadme(moduleContents, readmeContents, readmeFile.getName());
          updateSetupPartForProductModuleContent(product, moduleContents,
              productModuleContent.getTag());
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
        setupContent = updateImagesWithDownloadUrl(product, setupImagesFolder, setupContent);
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

  public String updateImagesWithDownloadUrl(Product product, List<GHContent> contents, String readmeContents) {
    List<GHContent> allContentOfImages = getAllImagesFromProductFolder(contents);
    Map<String, String> imageUrls = new HashMap<>();

    allContentOfImages.forEach(content -> Optional.of(imageService.mappingImageFromGHContent(product, content, false))
        .ifPresent(image -> imageUrls.put(content.getName(), IMAGE_ID_PREFIX.concat(image.getId()))));
    return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
  }

  private List<GHContent> getAllImagesFromProductFolder(List<GHContent> productFolderContents) {
    List<GHContent> images = new ArrayList<>();
    GitHubUtils.findImages(productFolderContents, images);
    return images;
  }

  private List<GHContent> getProductFolderContents(Product product, GHRepository ghRepository, String tag)
      throws IOException {
    String productFolderPath = ghRepository.getDirectoryContent(CommonConstants.SLASH, tag).stream()
        .filter(GHContent::isDirectory).map(GHContent::getName)
        .filter(content -> content.endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findFirst().orElse(null);
    productFolderPath = NonStandardProduct.findById(product.getId(), productFolderPath);

    return ghRepository.getDirectoryContent(productFolderPath, tag);
  }

  public String replaceVariable(String readmeContent, Product product, String tag) throws IOException {
    GHRepository ghRepository = gitHubService.getRepository(product.getRepositoryName());

    Function<Stream<GHContent>, GHContent> getPomFile = ghContents -> ghContents
        .filter(GHContent::isFile)
        .filter(content -> content.getName().equalsIgnoreCase(GitHubConstants.POM_FILE))
        .findFirst()
        .orElse(null);

    return Optional.ofNullable(ghRepository)
        .map(ghRepo -> getFolderContentByPath(ghRepo, CommonConstants.SLASH, tag))
        .map(ghContents -> filterProductFolderContent(ghContents, product.getId()))
        .map(productFolderContent -> getFolderContentByPath(ghRepository, productFolderContent.getName(), tag))
        .map(Collection::stream)
        .map(getPomFile)
        .filter(ObjectUtils::isNotEmpty)
        .map(pomFile -> readAndMapThePomFile(pomFile, ghRepository, readmeContent, tag))
        .orElse(readmeContent);
  }

  private GHContent filterProductFolderContent(List<GHContent> ghContents, String productId) {
    List<GHContent> productFolderContents = ghContents.stream()
        .filter(GHContent::isDirectory)
        .filter(content -> content.getName().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX))
        .toList();

    if (productFolderContents.size() > 1) {
      return productFolderContents.stream()
          .filter(content -> content.getName().contains(productId))
          .findFirst()
          .orElse(null);
    }
    return productFolderContents.isEmpty() ? null : productFolderContents.get(0);
  }

  private String readAndMapThePomFile(GHContent pomFile, GHRepository ghRepository, String readmeContent,
      String tag) {
    try {
      String pomContent = new String(pomFile.read().readAllBytes());
      String variableFilePathFromPomXML = extractVariableFilePathFromPomXML(pomContent);
      GHContent variableFile = ghRepository.getFileContent(variableFilePathFromPomXML, tag);
      String variableValue = new String(variableFile.read().readAllBytes());
      return StringUtils.isNotBlank(variableValue) ?
          readmeContent.replace(ReadmeConstants.VARIABLE_DIR, variableValue) : readmeContent;
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    return readmeContent;
  }

  private List<GHContent> getFolderContentByPath(GHRepository ghRepository, String path, String tag) {
    try {
      return ghRepository.getDirectoryContent(path, tag);
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }

  private String extractVariableFilePathFromPomXML(String content) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new java.io.ByteArrayInputStream(content.getBytes()));

      // Get the properties element by its tag name
      NodeList propertiesList = document.getElementsByTagName(GitHubConstants.VARIABLES_FILE_DIR);
      if (propertiesList.getLength() > 0) {
        String variableFileValue = propertiesList.item(0).getTextContent();
        return variableFileValue.replace(GitHubConstants.VARIABLES_PARENT_PATH, Strings.EMPTY);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }
}
