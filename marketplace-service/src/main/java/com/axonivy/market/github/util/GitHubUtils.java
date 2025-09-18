package com.axonivy.market.github.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.util.MavenUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHObject;
import org.kohsuke.github.PagedIterable;
import org.springframework.hateoas.Link;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubUtils {
  private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(CommonConstants.IMAGE_EXTENSION);

  public static long getGHCommitDate(GHCommit commit) {
    var commitTime = 0L;
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

  public static int sortMetaJsonFirst(String fileName1, String fileName2) {
    if (fileName1.endsWith(META_FILE)) {
      return -1;
    }
    if (fileName2.endsWith(META_FILE)) {
      return 1;
    }
    return fileName1.compareTo(fileName2);
  }

  public static void findImages(Iterable<GHContent> files, List<GHContent> images) {
    for (GHContent file : files) {
      if (file.isDirectory()) {
        findImagesInDirectory(file, images);
      } else if (IMAGE_EXTENSION_PATTERN.matcher(file.getName().toLowerCase(Locale.getDefault())).matches()) {
        images.add(file);
      }
    }
  }

  private static void findImagesInDirectory(GHContent file, List<GHContent> images) {
    try {
      List<GHContent> childrenFiles = file.listDirectoryContent().toList();
      findImages(childrenFiles, images);
    } catch (IOException e) {
      log.error("Error finding images in directory: ", e);
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
    if (content == null) {
      log.warn("Can not read the current content because it is null");
      return null;
    }
    try {
      return content.read();
    } catch (IOException e) {
      log.warn("Can not read the current content: {}", e.getMessage());
      return null;
    }
  }

  public static Link createSelfLinkForGithubReleaseModel(Product product, GHObject ghRelease) throws IOException {
    return linkTo(
        methodOn(ProductDetailsController.class).findGithubPublicReleaseByProductIdAndReleaseId(product.getId(),
            ghRelease.getId())).withSelfRel();
  }
}
