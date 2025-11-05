package com.axonivy.market.util;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.enums.DocumentLanguage;

import io.micrometer.common.util.StringUtils;

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
        var matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group(VERSION_INDEX);
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

  public static String updateVersionAndLanguageInPath(String productId, String artifactName, String bestMatch,
        DocumentLanguage language) {
        return SLASH + DirectoryConstants.CACHE_DIR + SLASH + productId + SLASH + artifactName + SLASH + bestMatch +
            SLASH + DirectoryConstants.DOC_DIR +SLASH + language.getCode() + SLASH + CommonConstants.INDEX_HTML;
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