package com.axonivy.market.comparator;

import com.axonivy.market.constants.CommonConstants;
import org.apache.commons.lang3.math.NumberUtils;
import org.kohsuke.github.GHTag;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.MavenConstants.SNAPSHOT_VERSION;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class MavenVersionComparator {

    private static final String MAIN_VERSION_REGEX = "\\.";

    private MavenVersionComparator() {
    }

    public static GHTag findHighestTag(List<GHTag> ghTags) {
        if (CollectionUtils.isEmpty(ghTags)) {
            return null;
        }
        String highestVersion = findHighestMavenVersion(ghTags.stream().map(GHTag::getName).toList());
        return ghTags.stream().filter(tag -> tag.getName().equals(highestVersion)).findAny().orElse(null);
    }

    public static String findHighestMavenVersion(List<String> versions) {
        if (CollectionUtils.isEmpty(versions)) {
            return null;
        }

        String highestVersion = versions.get(0);
        for (var version : versions) {
            if (compare(version, highestVersion) > 0) {
                highestVersion = version;
            }
        }
        return highestVersion;
    }

    private static int compare(String version, String otherVersion) {
        version = stripLeadingChars(version);
        otherVersion = stripLeadingChars(otherVersion);

        // Split versions into main parts and qualifiers
        String[] versionParts = version.split(CommonConstants.DASH_SEPARATOR, 2);
        String[] otherVersionParts = otherVersion.split(CommonConstants.DASH_SEPARATOR, 2);

        // Compare main version parts
        int mainComparison = compareMainVersion(versionParts[0], otherVersionParts[0]);
        if (mainComparison != 0) {
            return mainComparison;
        }

        // Compare qualifiers, if any
        String qualifier1 = versionParts.length > 1 ? versionParts[1] : EMPTY;
        String qualifier2 = otherVersionParts.length > 1 ? otherVersionParts[1] : EMPTY;

        // Consider versions without a qualifier higher than those with qualifiers
        if (qualifier1.isEmpty() && !qualifier2.isEmpty()) {
            return 1;
        }
        if (!qualifier1.isEmpty() && qualifier2.isEmpty()) {
            return -1;
        }

        return compareQualifier(qualifier1, qualifier2);
    }

    private static String stripLeadingChars(String version) {
        Pattern pattern = Pattern.compile(CommonConstants.DIGIT_REGEX);
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return version;
    }

    private static int compareMainVersion(String mainVersion, String otherMainVersion) {
        String[] parts1 = mainVersion.split(MAIN_VERSION_REGEX);
        String[] parts2 = otherMainVersion.split(MAIN_VERSION_REGEX);

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseToNumber(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseToNumber(parts2[i]) : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    private static int parseToNumber(String version) {
        if (NumberUtils.isDigits(version)) {
            return NumberUtils.toInt(version);
        }
        return 0;
    }

    private static int compareQualifier(String qualifier1, String qualifier2) {
        if (SNAPSHOT_VERSION.equals(qualifier1) && !SNAPSHOT_VERSION.equals(qualifier2)) {
            return -1;
        }
        if (!SNAPSHOT_VERSION.equals(qualifier1) && SNAPSHOT_VERSION.equals(qualifier2)) {
            return 1;
        }
        return qualifier1.compareTo(qualifier2);
    }
}
