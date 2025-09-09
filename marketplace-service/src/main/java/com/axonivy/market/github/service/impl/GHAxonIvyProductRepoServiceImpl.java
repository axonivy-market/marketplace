package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.ProductContentUtils;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.axonivy.market.constants.CommonConstants.IMAGE_ID_PREFIX;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
  private final GitHubService gitHubService;
  private final ImageService imageService;

  @SuppressWarnings("java:S3749")
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
          mapDescriptionSetupAndDemoToProductModuleContent(product, contents, readmeFile, moduleContents);
        }
        ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
      }
    } catch (Exception e) {
      log.error("Cannot get README file's content {}", e.getMessage());
    }
  }

  private void mapDescriptionSetupAndDemoToProductModuleContent(Product product, List<GHContent> contents,
      GHContent readmeFile,
      Map<String, Map<String, String>> moduleContents) throws IOException {
    var readmeContents = new String(readmeFile.read().readAllBytes(), StandardCharsets.UTF_8);
    if (ProductContentUtils.hasImageDirectives(readmeContents)) {
      readmeContents = updateImagesWithDownloadUrl(product.getId(), contents, readmeContents);
    }

    var readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);

    ProductContentUtils.mappingDescriptionSetupAndDemo(moduleContents, readmeFile.getName(), readmeContentsModel);
  }

  public String updateImagesWithDownloadUrl(String productId, List<GHContent> contents, String readmeContents) {
    List<GHContent> allContentOfImages = getAllImagesFromProductFolder(contents);
    Map<String, String> imageUrls = new HashMap<>();

    allContentOfImages.forEach(content -> Optional.of(imageService.mappingImageFromGHContent(productId, content))
        .ifPresent(image -> imageUrls.put(content.getName(), IMAGE_ID_PREFIX.concat(image.getId()))));
    return ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);
  }

  private static List<GHContent> getAllImagesFromProductFolder(List<GHContent> productFolderContents) {
    List<GHContent> images = new ArrayList<>();
    GitHubUtils.findImages(productFolderContents, images);
    return images;
  }
}
