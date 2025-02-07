package com.axonivy.market.github.util;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.util.MavenUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MetaConstants.META_FILE;

import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubUtils {

  public static long getGHCommitDate(GHCommit commit) {
    long commitTime = 0L;
    if (commit != null) {
      try {
        commitTime = commit.getCommitDate().getTime();
      } catch (Exception e) {
        log.error("Check last commit failed", e);
      }
    }
    return commitTime;
  }

  public static String getDownloadUrl(GHContent content) {
    try {
      return content.getDownloadUrl();
    } catch (IOException e) {
      log.error("Cannot get DownloadURl from GHContent: ", e);
    }
    return StringUtils.EMPTY;
  }

  public static <T> List<T> mapPagedIteratorToList(PagedIterable<T> paged) {
    if (paged != null) {
      try {
        return paged.toList();
      } catch (IOException e) {
        log.error("Cannot parse to list for pagediterable: ", e);
      }
    }
    return List.of();
  }

  public static String convertArtifactIdToName(String artifactId) {
    if (StringUtils.isBlank(artifactId)) {
      return StringUtils.EMPTY;
    }
    return Arrays.stream(artifactId.split(CommonConstants.DASH_SEPARATOR))
        .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
        .collect(Collectors.joining(CommonConstants.SPACE_SEPARATOR));
  }

  public static int sortMetaJsonFirst(String fileName1, String fileName2) {
    if (fileName1.endsWith(META_FILE))
      return -1;
    if (fileName2.endsWith(META_FILE))
      return 1;
    return fileName1.compareTo(fileName2);
  }

  public static void findImages(List<GHContent> files, List<GHContent> images) {
    for (GHContent file : files) {
      if (file.isDirectory()) {
        findImagesInDirectory(file, images);
      } else if (file.getName().toLowerCase().matches(CommonConstants.IMAGE_EXTENSION)) {
        images.add(file);
      }
    }
  }

  private static void findImagesInDirectory(GHContent file, List<GHContent> images) {
    try {
      List<GHContent> childrenFiles = file.listDirectoryContent().toList();
      findImages(childrenFiles, images);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  public static List<Artifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
    InputStream contentStream = extractedContentStream(content);
    if (Objects.isNull(contentStream)) {
      return new ArrayList<>();
    }
    return MavenUtils.extractMavenArtifactsFromContentStream(contentStream);
  }

  public static InputStream extractedContentStream(GHContent content) {
    try {
      return content.read();
    } catch (IOException | NullPointerException e) {
      log.warn("Can not read the current content: {}", e.getMessage());
      return null;
    }
  }

  public static Link createSelfLinkForGithubReleaseModel(Product product, GHRelease ghRelease) throws IOException {
    return linkTo(methodOn(ProductDetailsController.class).findGithubPublicReleaseByProductIdAndReleaseId(product.getId(),
        ghRelease.getId())).withSelfRel();
  }
}
