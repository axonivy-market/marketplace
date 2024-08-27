package com.axonivy.market.util;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.productjsonfilecontent.Data;
import com.axonivy.market.entity.productjsonfilecontent.Dependency;
import com.axonivy.market.entity.productjsonfilecontent.Installer;
import com.axonivy.market.entity.productjsonfilecontent.ProductJsonContent;
import com.axonivy.market.entity.productjsonfilecontent.Project;
import com.axonivy.market.enums.NonStandardProduct;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.axonivy.market.constants.ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID;
import static com.axonivy.market.constants.ProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID;
import static com.axonivy.market.constants.ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID;
import static com.axonivy.market.constants.ProductJsonConstants.VERSION_VALUE;

public class VersionUtils {
    private VersionUtils() {
    }
    public static List<String> getVersionsToDisplay(List<String> versions, Boolean isShowDevVersion, String designerVersion) {
        Stream<String> versionStream = versions.stream();
        if (StringUtils.isNotBlank(designerVersion)) {
            return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).toList();
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
            LatestVersionComparator comparator = new LatestVersionComparator();
            bestMatchVersion = versions.stream().filter(version -> comparator.compare(version, designerVersion) > 0 && isReleasedVersion(version)).findAny().orElse(null);
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
        if (isSnapshotVersion(version)) {
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

    public static boolean isReleasedVersion(String version) {
        return !(isSprintVersion(version) || isSnapshotVersion(version));
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

    public static void updateVersionForInstaller(ProductJsonContent productJsonContent, String tag) {
        if (ObjectUtils.isEmpty(productJsonContent.getInstallers())) {
            return;
        }
        for (Installer installer : productJsonContent.getInstallers()) {
            String installId = installer.getId();
            switch (installId) {
            case MAVEN_IMPORT_INSTALLER_ID:
                updateVersionForInstallForMavenImport(installer, tag);
                break;
            case MAVEN_DEPENDENCY_INSTALLER_ID, MAVEN_DROPINS_INSTALLER_ID:
                updateVersionForInstallForMavenDependencyAndDropins(installer, tag);
                break;
            default:
                break;
            }
        }
    }

    private static void updateVersionForInstallForMavenImport(Installer installer, String tag) {
        Optional.of(installer).map(Installer::getData).map(Data::getProjects).ifPresent(projects -> {
            for (Project project : projects) {
                if (VERSION_VALUE.equals(project.getVersion())) {
                    project.setVersion(tag);
                }
            }
        });
    }

    private static void updateVersionForInstallForMavenDependencyAndDropins(Installer installer, String tag) {
        Optional.of(installer).map(Installer::getData).map(Data::getDependencies).ifPresent(dependencies -> {
            for (Dependency dependency : dependencies) {
                if (VERSION_VALUE.equals(dependency.getVersion())) {
                    dependency.setVersion(tag);
                }
            }
        });
    }

}
