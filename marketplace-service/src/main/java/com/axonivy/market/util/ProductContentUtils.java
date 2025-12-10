package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.model.ReadmeContentsModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHRelease;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.ProductJsonConstants.DEFAULT_PRODUCT_TYPE;

public final class ProductContentUtils {
  /*
   * Accept any combination of #, can be ## or ###, and whitespaces before Demo/Setup word
   * Match exactly Demo or Setup
   */
  private static final String HASH = "#";
  public static final String DESCRIPTION = "description";
  public static final String DEMO = "demo";
  public static final String SETUP = "setup";
  public static final String README_IMAGE_FORMAT = "\\(([^)]*?/)?%s(\\s+\"[^\"]+\")?\\)";
  public static final String IMAGE_DOWNLOAD_URL_FORMAT = "(%s)";
  private static final String FIRST_REGEX_CAPTURING_GROUP = "$1";
  private static final String GITHUB_PULL_REQUEST_NUMBER_REGEX = "#(\\d+)";
  private static final String GITHUB_USERNAME_REGEX = "@([\\p{Alnum}\\-]+)";
  private static final String GITHUB_PULL_REQUEST_LINK = "/pull/";
  private static final String GITHUB_MAIN_LINK = "https://github.com/";
  private static final Pattern GITHUB_PULL_REQUEST_PATTERN = Pattern.compile(GITHUB_PULL_REQUEST_NUMBER_REGEX);
  private static final Pattern GITHUB_USERNAME_PATTERN = Pattern.compile(GITHUB_USERNAME_REGEX,
      Pattern.UNICODE_CHARACTER_CLASS);
  public static final Pattern README_FILE_LOCALE_PATTERN =
      Pattern.compile(GitHubConstants.README_FILE_LOCALE_REGEX);
  public static final Pattern IMAGE_EXTENSION_PATTERN =
      Pattern.compile(CommonConstants.IMAGE_EXTENSION);

  private ProductContentUtils() {
  }

  /**
   * MARP-810: Sabine requires that content in other languages, which has not been translated, be left empty and
   * replaced with English content.
   */
  public static Map<String, String> replaceEmptyContentsWithEnContent(Map<String, String> map) {
    String enValue = map.get(Language.EN.getValue());
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (StringUtils.isBlank(entry.getValue())) {
        map.put(entry.getKey(), enValue);
      }
    }
    return map;
  }

  public static String getReadmeFileLocale(String readmeFile) {
    String result = StringUtils.EMPTY;
    var matcher = README_FILE_LOCALE_PATTERN.matcher(readmeFile);
    if (matcher.find()) {
      result = matcher.group(1);
    }
    return result;
  }

  // Cover some cases including when demo and setup parts switch positions or
  // missing one of them
  public static ReadmeContentsModel getExtractedPartsOfReadme(String readmeContents) {
    int demoStart = findSectionStart(ReadmeConstants.DEMO_PATTERN, readmeContents);
    int setupStart = findSectionStart(ReadmeConstants.SETUP_PATTERN, readmeContents);

    String description;
    String demo = Strings.EMPTY;
    String setup = Strings.EMPTY;

    if (demoStart == -1 && setupStart == -1) {
      description = removeFirstLine(readmeContents);
    } else if (isDemoFirst(demoStart, setupStart)) {
      description = removeFirstLine(readmeContents.substring(0, demoStart));
      demo = extractSection(readmeContents, demoStart, ReadmeConstants.DEMO_PATTERN, setupStart);
      setup = setupStart != -1 ? extractSection(readmeContents, setupStart, ReadmeConstants.SETUP_PATTERN,
          -1) : Strings.EMPTY;
    } else {
      description = removeFirstLine(readmeContents.substring(0, setupStart));
      setup = extractSection(readmeContents, setupStart, ReadmeConstants.SETUP_PATTERN, demoStart);
      demo = demoStart != -1 ? extractSection(readmeContents, demoStart, ReadmeConstants.DEMO_PATTERN,
          -1) : Strings.EMPTY;
    }

    var model = new ReadmeContentsModel();
    model.setDescription(description.trim());
    model.setDemo(demo.trim());
    model.setSetup(setup.trim());
    return model;
  }

  private static int findSectionStart(Pattern pattern, String content) {
    var matcher = pattern.matcher(content);
    return matcher.find() ? matcher.start() : -1;
  }

  private static boolean isDemoFirst(int demoStart, int setupStart) {
    return demoStart != -1 && (setupStart == -1 || demoStart < setupStart);
  }

  private static String extractSection(String content, int start, Pattern pattern, int nextSectionStart) {
    var matcher = pattern.matcher(content);
    if (!matcher.find(start)) {
      return Strings.EMPTY;
    }
    int sectionHeaderEnd = start + matcher.group().length();
    int end = nextSectionStart != -1 ? nextSectionStart : content.length();
    return content.substring(sectionHeaderEnd, end).trim();
  }

  public static boolean hasImageDirectives(String readmeContents) {
    var matcher = IMAGE_EXTENSION_PATTERN.matcher(readmeContents);
    return matcher.find();
  }

  public static String removeFirstLine(String text) {
    String result;
    if (StringUtils.isBlank(text)) {
      result = Strings.EMPTY;
    } else if (text.startsWith(HASH)) {
      int index = text.indexOf(StringUtils.LF);
      if (index != StringUtils.INDEX_NOT_FOUND) {
        result = text.substring(index + 1).trim();
      } else {
        result = Strings.EMPTY;
      }
    } else {
      result = text;
    }

    return result;
  }

  public static ProductModuleContent initProductModuleContent(String productId, String version) {
    var productModuleContent = new ProductModuleContent();
    productModuleContent.setProductId(productId);
    productModuleContent.setVersion(version);
    ProductFactory.mappingIdForProductModuleContent(productModuleContent);
    return productModuleContent;
  }

  public static void updateProductModule(ProductModuleContent productModuleContent, Collection<Artifact> artifacts) {
    var artifact = artifacts.stream().filter(Artifact::getIsDependency).findFirst().orElse(null);
    if (Objects.nonNull(artifact)) {
      productModuleContent.setIsDependency(Boolean.TRUE);
      productModuleContent.setGroupId(artifact.getGroupId());
      productModuleContent.setArtifactId(artifact.getArtifactId());
      productModuleContent.setType(StringUtils.defaultIfBlank(artifact.getType(), DEFAULT_PRODUCT_TYPE));
      productModuleContent.setName(artifact.getName());
    }
  }

  public static void updateProductModuleTabContents(ProductModuleContent productModuleContent,
      Map<String, Map<String, String>> moduleContents) {
    productModuleContent.setDescription(replaceEmptyContentsWithEnContent(moduleContents.get(DESCRIPTION)));
    productModuleContent.setDemo(replaceEmptyContentsWithEnContent(moduleContents.get(DEMO)));
    productModuleContent.setSetup(replaceEmptyContentsWithEnContent(moduleContents.get(SETUP)));
  }

  /**
   * Cover some inconsistent cases:
   * Products contain image names in round brackets (employee-onboarding, demo-projects, etc.)
   * Image with name contains in other images' (mattermost)
   */
  public static String replaceImageDirWithImageCustomId(Map<String, String> imageUrls, String readmeContents) {
    for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
      var imagePattern = String.format(README_IMAGE_FORMAT, Pattern.quote(entry.getKey()));
      readmeContents = readmeContents.replaceAll(imagePattern,
          String.format(IMAGE_DOWNLOAD_URL_FORMAT, entry.getValue()));
    }
    return readmeContents;
  }

  public static void mappingDescriptionSetupAndDemo(Map<String, Map<String, String>> moduleContents,
      String readmeFileName, ReadmeContentsModel readmeContentsModel) {
    String locale = Optional.ofNullable(getReadmeFileLocale(readmeFileName))
        .filter(StringUtils::isNotEmpty)
        .map(String::toLowerCase)
        .orElse(Language.EN.getValue());

    moduleContents.computeIfAbsent(DESCRIPTION, key -> new HashMap<>()).put(locale,
        readmeContentsModel.getDescription());
    moduleContents.computeIfAbsent(SETUP, key -> new HashMap<>()).put(locale, readmeContentsModel.getSetup());
    moduleContents.computeIfAbsent(DEMO, key -> new HashMap<>()).put(locale, readmeContentsModel.getDemo());
  }

  public static List<GHRelease> extractReleasesPage(List<GHRelease> ghReleases, Pageable pageable) {
    int start = pageable.getPageNumber() * pageable.getPageSize();
    if (start >= ghReleases.size()) {
      return Collections.emptyList();
    }
    int end = Math.min(start + pageable.getPageSize(), ghReleases.size());
    return ghReleases.subList(start, end);
  }

  public static String transformGithubReleaseBody(String githubReleaseBody, String productSourceUrl) {
    var body = StringUtils.defaultString(githubReleaseBody);
    body = GITHUB_PULL_REQUEST_PATTERN.matcher(body).replaceAll(
        productSourceUrl + GITHUB_PULL_REQUEST_LINK + FIRST_REGEX_CAPTURING_GROUP);
    body = GITHUB_USERNAME_PATTERN.matcher(body).replaceAll(GITHUB_MAIN_LINK + FIRST_REGEX_CAPTURING_GROUP);
    return body;
  }
}
