package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.ProductContentUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public GHAxonIvyProductRepoServiceImpl(GitHubService gitHubService, ImageService imageService) {
    this.gitHubService = gitHubService;
    this.imageService = imageService;
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
          updateSetupPartForProductModuleContent(product, moduleContents,
              productModuleContent.getTag());
        }
        ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
      }
    } catch (Exception e) {
      log.error("Cannot get README file's content {}", e.getMessage());
    }
  }

  //TODO: Check employee boarding
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
}
