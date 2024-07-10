package com.axonivy.market.github.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.NonStandardProductPackageConstants;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.PagedIterable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubUtils {

  private static String pathToProductFolderFromTagContent;
  private static String pathToImageFolder;

  public static long getGHCommitDate(GHCommit commit) {
    long commitTime = 0l;
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
    return "";
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

  public static String getNonStandardProductFilePath(String productId) {
    switch (productId) {
      case NonStandardProductPackageConstants.PORTAL:
        pathToProductFolderFromTagContent = "AxonIvyPortal/portal-product";
        break;
      case NonStandardProductPackageConstants.CONNECTIVITY_FEATURE:
        pathToProductFolderFromTagContent = "connectivity/connectivity-demos-product";
        break;
      case NonStandardProductPackageConstants.ERROR_HANDLING:
        pathToProductFolderFromTagContent = "error-handling/error-handling-demos-product";
        break;
      case NonStandardProductPackageConstants.WORKFLOW_DEMO:
        pathToProductFolderFromTagContent = "workflow/workflow-demos-product";
        break;
      case NonStandardProductPackageConstants.MICROSOFT_365:
        pathToProductFolderFromTagContent = "msgraph-connector-product/products/msgraph-connector";
        break;
      case NonStandardProductPackageConstants.MICROSOFT_CALENDAR:
        pathToProductFolderFromTagContent = "msgraph-connector-product/products/msgraph-calendar";
        break;
      case NonStandardProductPackageConstants.MICROSOFT_TEAMS:
        pathToProductFolderFromTagContent = "msgraph-connector-product/products/msgraph-chat";
        break;
      case NonStandardProductPackageConstants.MICROSOFT_MAIL:
        pathToProductFolderFromTagContent = "msgraph-connector-product/products/msgraph-mail";
        break;
      case NonStandardProductPackageConstants.MICROSOFT_TODO:
        pathToProductFolderFromTagContent = "msgraph-connector-product/products/msgraph-todo";
        break;
      case NonStandardProductPackageConstants.HTML_DIALOG_DEMO:
        pathToProductFolderFromTagContent = "html-dialog/html-dialog-demos-product";
        break;
      case NonStandardProductPackageConstants.RULE_ENGINE_DEMOS:
        pathToProductFolderFromTagContent = "rule-engine/rule-engine-demos-product";
        break;
      case NonStandardProductPackageConstants.OPENAI_CONNECTOR:
        pathToProductFolderFromTagContent = "openai-connector-product";
        break;
      case NonStandardProductPackageConstants.OPENAI_ASSISTANT:
        pathToProductFolderFromTagContent = "openai-assistant-product";
        break;
      default:
        break;
    }
    return pathToProductFolderFromTagContent;
  }

  public static String getNonStandardImageFolder(String productId) {
    switch (productId) {
      case NonStandardProductPackageConstants.EXCEL_IMPORTER:
        pathToImageFolder = "doc";
        break;
      case NonStandardProductPackageConstants.EXPRESS_IMPORTER, NonStandardProductPackageConstants.DEEPL_CONNECTOR:
        pathToImageFolder = "img";
        break;
      case NonStandardProductPackageConstants.GRAPHQL_DEMO:
        pathToImageFolder = "assets";
        break;
      case NonStandardProductPackageConstants.OPENAI_ASSISTANT:
        pathToImageFolder = "docs";
        break;
      default:
        pathToImageFolder = "images";
        break;
    }
    return pathToImageFolder;
  }
}
