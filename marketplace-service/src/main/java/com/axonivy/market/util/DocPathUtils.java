package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.enums.DocumentLanguage;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.SLASH;

public final class DocPathUtils {
  private static final Pattern PATH_PATTERN =
      Pattern.compile("^/?([^/]+)/([^/]+)/([^/]+)(?:/(.*))?$");
  private static final int VERSION_INDEX = 1;
  private static final int PRODUCT_ID_INDEX = 3;
  private static final int ARTIFACT_INDEX = 2;
  public static final String DOC_FACTORY_DOC = "docfactory";
  public static final String DOC_FACTORY_ID = "doc-factory";
  public static final String DOC_EXTENSION = "-doc";
  public static final String PORTAL_ID = "portal";
  public static final String GUIDE_EXTENSION = "-guide";

  private DocPathUtils() {
  }

  /**
   * Extract the productId from a path like:
   * /portal/portal-guide/13.1.1/doc/_images/dashboard1.png -> portal
   * /doc-factory/doc-factory-doc/13.2.1 -> docfactory
   */
  public static String extractProductId(String path) {
    var matcher = PATH_PATTERN.matcher(path);
    if (matcher.matches()) {
      String productId = matcher.group(VERSION_INDEX);
      return StringUtils.isNotBlank(productId) && productId.equalsIgnoreCase(DOC_FACTORY_ID) ?
          DOC_FACTORY_DOC: productId;
    }
    return null;
  }

  /**
   * Get the normalized product ID, converting docfactory to doc-factory if needed
   */
  public static String getProductName(String productId) {
    if (productId != null && productId.equalsIgnoreCase(DOC_FACTORY_DOC)) {
      return DOC_FACTORY_ID;
    }
    return productId;
  }

  /**
   * Extract the version from a path like:
   * /portal/portal-guide/13.1.1/doc/_images/dashboard1.png -> 13.1.1
   */
  public static String extractVersion(String path) {
    var matcher = PATH_PATTERN.matcher(path);
    if (matcher.matches()) {
      return matcher.group(PRODUCT_ID_INDEX);
    }
    return null;
  }

  /**
   * Generate the document path based on given parameters
   */
  public static String generatePath(String productId, String artifactName, String bestMatch,
      DocumentLanguage language) {
    return String.join(SLASH,
        StringUtils.EMPTY,
        DirectoryConstants.CACHE_DIR,
        productId,
        artifactName,
        bestMatch,
        DirectoryConstants.DOC_DIR,
        language.getCode(),
        CommonConstants.INDEX_HTML
    );
  }

  /**
   * Extract the artifact name from a path like:
   * /portal/portal-guide/13.1.1/doc/_images/dashboard1.png -> portal-guide
   */
  public static String extractArtifactName(String path) {
    var matcher = PATH_PATTERN.matcher(path);
    if (matcher.matches()) {
      return matcher.group(ARTIFACT_INDEX);
    }
    return null;
  }

  /**
   * Create the artifact name by product Name
   * portal -> portal-guide
   * docfactory -> doc-factory-doc
   * doc-factory -> doc-factory-doc
   */
  public static String createArtifactNameByProductName(String productName) {
    if (StringUtils.isBlank(productName)) {
      return null;
    }

    String lowerCaseProductName = productName.toLowerCase(Locale.ENGLISH);
    return switch (lowerCaseProductName) {
      case PORTAL_ID -> PORTAL_ID + GUIDE_EXTENSION;
      case DOC_FACTORY_DOC -> DOC_FACTORY_ID + DOC_EXTENSION;
      default -> lowerCaseProductName + DOC_EXTENSION;
    };
  }

  /**
   * Extract the language from a path that contains language code.
   * Example: For path containing "doc/en/index.html", returns DocumentLanguage.ENGLISH
   *
   * @param path The path to extract language from
   * @return The DocumentLanguage if found, null otherwise
   */
  public static DocumentLanguage extractLanguage(String path) {
    if (StringUtils.isBlank(path)) {
      return null;
    }

    return Arrays.stream(path.split(SLASH))
        .filter(segment -> DocumentLanguage.getCodes().contains(segment))
        .map(DocumentLanguage::fromCode)
        .findFirst()
        .orElse(null);
  }
}