package com.axonivy.market.util;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.NonStandardProduct;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHTag;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class VersionUtils {
    public static final String NON_NUMERIC_CHAR = "[^0-9.]";

    private VersionUtils() {
    }
    public static List<String> getVersionsToDisplay(List<String> versions, Boolean isShowDevVersion, String designerVersion) {
        Stream<String> versionStream = versions.stream();
        if (StringUtils.isNotBlank(designerVersion)) {
            return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).sorted(new LatestVersionComparator()).toList();
        }
        if (BooleanUtils.isTrue(isShowDevVersion)) {
            return versionStream.filter(version -> isOfficialVersionOrUnReleasedDevVersion(versions, version))
                    .sorted(new LatestVersionComparator()).toList();
        }
        return versions.stream().filter(VersionUtils::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
    }

    public static String getBestMatchVersion(List<String> versions, String designerVersion) {
        String bestMatchVersion = versions.stream().filter(version -> StringUtils.equals(version, designerVersion)).findAny().orElse(null);
        if(StringUtils.isBlank(bestMatchVersion)){
            bestMatchVersion = versions.stream().filter(version -> MavenVersionComparator.compare(version, designerVersion) < 0 && isReleasedVersion(version)).findAny().orElse(null);
        }
        if (StringUtils.isBlank(bestMatchVersion)) {
            bestMatchVersion = versions.stream().filter(VersionUtils::isReleasedVersion).findAny().orElse(CollectionUtils.firstElement(versions));
        }
        return bestMatchVersion;
    }

    public static boolean isOfficialVersionOrUnReleasedDevVersion(List<String> versions, String version) {
        if (isReleasedVersion(version)) {
            return true;
        }
        String bugfixVersion;
        if (!isValidFormatReleasedVersion(version)) {
            return false;
        } else if (isSnapshotVersion(version)) {
            bugfixVersion = getBugfixVersion(version.replace(MavenConstants.SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY));
        } else {
            bugfixVersion = getBugfixVersion(version.split(MavenConstants.SPRINT_RELEASE_POSTFIX)[0]);
        }
        return versions.stream().noneMatch(
                currentVersion -> !currentVersion.equals(version) && isReleasedVersion(currentVersion) && getBugfixVersion(
                        currentVersion).equals(bugfixVersion));
    }

    public static boolean isSnapshotVersion(String version) {
        return version.endsWith(MavenConstants.SNAPSHOT_RELEASE_POSTFIX);
    }

    public static boolean isSprintVersion(String version) {
        return version.contains(MavenConstants.SPRINT_RELEASE_POSTFIX);
    }

    public static boolean isValidFormatReleasedVersion(String version) {
        return StringUtils.isNumeric(version.split(MavenConstants.MAIN_VERSION_REGEX)[0]);
    }

    public static boolean isReleasedVersion(String version) {
        return !(isSprintVersion(version) || isSnapshotVersion(version)) && isValidFormatReleasedVersion(version);
    }

    public static boolean isMatchWithDesignerVersion(String version, String designerVersion) {
        return isReleasedVersion(version) && version.startsWith(designerVersion);
    }

    public static String getBugfixVersion(String version) {

        if (isSnapshotVersion(version)) {
            version = version.replace(MavenConstants.SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY);
        } else if (isSprintVersion(version)) {
            version = version.split(MavenConstants.SPRINT_RELEASE_POSTFIX)[0];
        }
        String[] segments = version.split("\\.");
        if (segments.length >= 3) {
            segments[2] = segments[2].split(CommonConstants.DASH_SEPARATOR)[0];
            return segments[0] + CommonConstants.DOT_SEPARATOR + segments[1] + CommonConstants.DOT_SEPARATOR + segments[2];
        }
        return version;
    }

    public static String convertTagToVersion (String tag){
        if(StringUtils.isBlank(tag) || !StringUtils.startsWith(tag, GitHubConstants.STANDARD_TAG_PREFIX)){
            return tag;
        }
        return tag.substring(1);
    }

    public static List<String> convertTagsToVersions (List<String> tags){
        Objects.requireNonNull(tags);
        return tags.stream().map(VersionUtils::convertTagToVersion).toList();
    }

    public static String convertVersionToTag(String productId, String version) {
        if (StringUtils.isBlank(version)) {
            return version;
        }
        NonStandardProduct product = NonStandardProduct.findById(productId);
        if (product.isVersionTagNumberOnly()) {
            return version;
        }
        return GitHubConstants.STANDARD_TAG_PREFIX.concat(version);
    }

    public static String getOldestVersion(List<GHTag> tags) {
        String result = StringUtils.EMPTY;
        if (!CollectionUtils.isEmpty(tags)) {
            List<String> releasedTags = tags.stream().map(tag -> tag.getName().replaceAll(NON_NUMERIC_CHAR, Strings.EMPTY))
                .distinct().sorted(new LatestVersionComparator()).toList();
            return CollectionUtils.lastElement(releasedTags);
        }
        return result;
    }
    public static List<String> getReleaseTagsFromProduct(Product product) {
        if (Objects.isNull(product) || CollectionUtils.isEmpty(product.getReleasedVersions())) {
            return new ArrayList<>();
        }
        return product.getReleasedVersions().stream().map(version -> convertVersionToTag(product.getId(), version)).toList();
    }
}
