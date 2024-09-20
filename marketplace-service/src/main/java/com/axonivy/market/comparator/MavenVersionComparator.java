package com.axonivy.market.comparator;

import com.axonivy.market.constants.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.kohsuke.github.GHTag;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.DASH_SEPARATOR;
import static com.axonivy.market.constants.MavenConstants.MAIN_VERSION_REGEX;
import static com.axonivy.market.constants.MavenConstants.SNAPSHOT_VERSION;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class MavenVersionComparator {

    private static final int GREATER_THAN = 1;
    private static final int EQUAL = 0;
    private static final int LESS_THAN = -1;

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
            if (compare(version, highestVersion) > EQUAL) {
                highestVersion = version;
            }
        }
        return highestVersion;
    }

    public static int compare(String version, String otherVersion) {
        version = stripLeadingChars(version);
        otherVersion = stripLeadingChars(otherVersion);
        String[] versionParts = createMainAndQualifierArray(version);
        String[] otherVersionParts = createMainAndQualifierArray(otherVersion);

        // Compare main version parts
        int mainComparison = compareMainVersion(versionParts[0], otherVersionParts[0]);
        if (mainComparison != EQUAL) {
            return mainComparison;
        }

        // Compare qualifiers
        String qualifier1 = getQualifierPart(versionParts);
        String qualifier2 = getQualifierPart(otherVersionParts);
        // Consider versions without a qualifier higher than those with qualifiers
        if (qualifier1.isEmpty() && !qualifier2.isEmpty()) {
            return GREATER_THAN;
        }
        if (!qualifier1.isEmpty() && qualifier2.isEmpty()) {
            return LESS_THAN;
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
            int num1 = parseToNumber(parts1, i);
            int num2 = parseToNumber(parts2, i);
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return EQUAL;
    }

    private static String getQualifierPart(String[] versionParts) {
        return versionParts.length > 1 ? versionParts[1] : EMPTY;
    }

    private static String[] createMainAndQualifierArray(String version) {
        return StringUtils.defaultIfBlank(version, EMPTY).split(DASH_SEPARATOR, 2);
    }

    private static int parseToNumber(String[] versionParts, int index) {
        if (index < versionParts.length && NumberUtils.isDigits(versionParts[index])) {
            return NumberUtils.toInt(versionParts[index]);
        }
        return 0;
    }

    private static int compareQualifier(String qualifier1, String qualifier2) {
        if (SNAPSHOT_VERSION.equals(qualifier1) && !SNAPSHOT_VERSION.equals(qualifier2)) {
            return LESS_THAN;
        }
        if (!SNAPSHOT_VERSION.equals(qualifier1) && SNAPSHOT_VERSION.equals(qualifier2)) {
            return GREATER_THAN;
        }
        return qualifier1.compareTo(qualifier2);
    }
}
