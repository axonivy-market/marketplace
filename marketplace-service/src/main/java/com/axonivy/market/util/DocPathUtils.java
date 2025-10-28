package com.axonivy.market.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.DirectoryConstants.DATA_CACHE_DIR;

public final class DocPathUtils {
    private static final Pattern PATH_PATTERN =
            Pattern.compile("^/?([^/]+)/([^/]+)/([^/]+)(?:/(.*))?$");
    private static final int VERSION_INDEX = 1;
    private static final int PRODUCT_ID_INDEX = 3;
    private static final String DOC_FACTORY_DOC = "docfactory";
    private static final String DOC_FACTORY_ID = "doc-factory";

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

    public static String updateVersionInPath(String path, String bestMatch, String version) {
        // Replace the old version with the best matched version
        return SLASH + path.replaceFirst(SLASH + Pattern.quote(version) + SLASH, SLASH + bestMatch + SLASH);
    }

    /**
     * Normalize the given path to prevent path traversal attacks.
     * Ensures the resulting path is within the DATA_CACHE_DIR.
     * Returns null if the path is invalid or attempts to traverse outside the base directory.
     */
    public static Path resolveDocPath(String path) {
        var baseDir = Paths.get(DATA_CACHE_DIR).toAbsolutePath().normalize();
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
}
