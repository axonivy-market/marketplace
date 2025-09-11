package com.axonivy.market.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.SLASH;

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

}
