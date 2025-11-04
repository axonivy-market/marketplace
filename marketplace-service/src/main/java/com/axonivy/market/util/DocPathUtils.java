package com.axonivy.market.util;

import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.enums.DocumentLanguage;

import io.micrometer.common.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
    private DocPathUtils() {
    }

    /**
     * Extract the productId from a path like:
     * /portal/portal-guide/13.1.1/doc/_images/dashboard1.png -> portal
     */
    public static String extractProductId(String path) {
      String productId = null;
      var matcher = PATH_PATTERN.matcher(path);
      if (matcher.matches()) {
        productId = matcher.group(VERSION_INDEX);
        if (productId.equalsIgnoreCase(DOC_FACTORY_DOC)){
          productId = DOC_FACTORY_ID;
        }
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

  public static String updateVersionAndLanguageInPath(String productId, String artifactName, String bestMatch,
        DocumentLanguage language) {
        return SLASH + DirectoryConstants.CACHE_DIR + SLASH + productId + SLASH + artifactName + SLASH + bestMatch + SLASH + DirectoryConstants.DOC_DIR +SLASH + language.getCode() + SLASH + "index.html";
    }

    /**
     * Normalize the given path to prevent path traversal attacks.
     * Ensures the resulting path is within the DATA_CACHE_DIR.
     * Returns null if the path is invalid or attempts to traverse outside the base directory.
     */
    public static Path resolveDocPath(String path) {
        var baseDir = Paths.get(DirectoryConstants.DATA_DIR).toAbsolutePath().normalize();
        var relativePath = Paths.get(path).normalize();
        if (relativePath.isAbsolute()) {
            relativePath = Paths.get(path.substring(1)).normalize();
        }
        var resolvedPath = baseDir.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(baseDir)) {
            return null;
        }
        return resolvedPath;
    }

    public static String extractArtifactName(String path) {
        var matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group(ARTIFACT_INDEX);
        }
        return null;
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