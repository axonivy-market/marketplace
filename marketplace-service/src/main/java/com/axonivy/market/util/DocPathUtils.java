package com.axonivy.market.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.DirectoryConstants.DATA_CACHE_DIR;

public class DocPathUtils {

    private DocPathUtils() {
    }

    private static final Pattern PATH_PATTERN =
            Pattern.compile("^/?([^/]+)/([^/]+)/([^/]+)(?:/(.*))?$");
    /**
     * Extract the productId from a path like:
     * /portal/portal-guide/13.1.1/doc/_images/dashboard1.png -> portal
     */
    public static String extractProductId(String path) {
        Matcher matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Extract the version from a path like:
     * /portal/portal-guide/13.1.1/doc/_images/dashboard1.png -> 13.1.1
     */
    public static String extractVersion(String path) {
        Matcher matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group(3);
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
        Path baseDir = Paths.get(DATA_CACHE_DIR).toAbsolutePath().normalize();
        Path relativePath = Paths.get(path).normalize();
        if (relativePath.isAbsolute()) {
            relativePath = Paths.get(path.substring(1)).normalize();
        }
        Path resolvedPath = baseDir.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(baseDir)) {
            return null;
        }
        return resolvedPath;
    }

}
