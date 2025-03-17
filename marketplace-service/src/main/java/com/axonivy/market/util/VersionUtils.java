package com.axonivy.market.util;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.model.MavenArtifactKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.constants.CommonConstants.DOT_SEPARATOR;
import static com.axonivy.market.constants.MavenConstants.*;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionUtils {
  public static final String NON_NUMERIC_CHAR = "[^0-9.]";

  public static List<String> getVersionsToDisplay(List<String> versions, Boolean isShowDevVersion,
      String designerVersion) {
    Stream<String> versionStream = versions.stream();
    if (StringUtils.isNotBlank(designerVersion)) {
      return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).sorted(
          new LatestVersionComparator()).toList();
    }
    if (BooleanUtils.isTrue(isShowDevVersion)) {
      return versionStream.filter(version -> isOfficialVersionOrUnReleasedDevVersion(versions, version))
          .sorted(new LatestVersionComparator()).toList();
    }
    return versions.stream().filter(VersionUtils::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
  }

  public static String getBestMatchVersion(List<String> versions, String designerVersion) {
    String bestMatchVersion = versions.stream().filter(
        version -> StringUtils.equals(version, designerVersion)).findAny().orElse(null);
    if (StringUtils.isBlank(bestMatchVersion)) {
      bestMatchVersion = versions.stream().filter(
          version -> MavenVersionComparator.compare(version, designerVersion) < 0 && isReleasedVersion(
              version)).findAny().orElse(null);
    }
    if (StringUtils.isBlank(bestMatchVersion)) {
      bestMatchVersion = versions.stream().filter(VersionUtils::isReleasedVersion).findAny().orElse(
          CollectionUtils.firstElement(versions));
    }
    return bestMatchVersion;
  }

  public static boolean isOfficialVersionOrUnReleasedDevVersion(Collection<String> versions, String version) {
    if (isReleasedVersion(version)) {
      return true;
    }
    String bugfixVersion;
    if (!isValidFormatReleasedVersion(version)) {
      return false;
    } else if (isSnapshotVersion(version)) {
      bugfixVersion = getBugfixVersion(version.replace(SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY));
    } else {
      bugfixVersion = getBugfixVersion(version.split(SPRINT_RELEASE_POSTFIX)[0]);
    }
    return versions.stream().noneMatch(
        currentVersion -> !currentVersion.equals(version) && isReleasedVersion(currentVersion) && getBugfixVersion(
            currentVersion).equals(bugfixVersion));
  }

  public static boolean isSnapshotVersion(String version) {
    return version.endsWith(SNAPSHOT_RELEASE_POSTFIX);
  }

  public static boolean isSprintVersion(String version) {
    return version.contains(SPRINT_RELEASE_POSTFIX);
  }

  public static boolean isValidFormatReleasedVersion(String version) {
    return StringUtils.isNumeric(version.split(MAIN_VERSION_REGEX)[0]);
  }

  public static boolean isReleasedVersion(String version) {
    return !(isSprintVersion(version) || isSnapshotVersion(version)) && isValidFormatReleasedVersion(version);
  }

  public static boolean isMatchWithDesignerVersion(String version, String designerVersion) {
    return isReleasedVersion(version) && version.startsWith(designerVersion);
  }

  public static String getBugfixVersion(String version) {
    if (isSnapshotVersion(version)) {
      version = version.replace(SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY);
    } else if (isSprintVersion(version)) {
      version = version.split(SPRINT_RELEASE_POSTFIX)[0];
    }
    String[] segments = version.split(MAIN_VERSION_REGEX);
    if (segments.length >= 3) {
      segments[2] = segments[2].split(CommonConstants.DASH_SEPARATOR)[0];
      return segments[0] + CommonConstants.DOT_SEPARATOR + segments[1] + CommonConstants.DOT_SEPARATOR + segments[2];
    }
    return version;
  }

  public static String getOldestVersions(List<String> versions) {
    String result = StringUtils.EMPTY;
    if (!CollectionUtils.isEmpty(versions)) {
      List<String> releasedVersions = versions.stream().map(
              version -> version.replaceAll(NON_NUMERIC_CHAR, Strings.EMPTY))
          .distinct().sorted(new LatestVersionComparator()).toList();
      return CollectionUtils.lastElement(releasedVersions);
    }
    return result;
  }

  public static List<String> removeSyncedVersionsFromReleasedVersions(List<String> releasedVersion,
      Set<String> syncTags) {
    if (ObjectUtils.isNotEmpty(syncTags)) {
      releasedVersion.removeAll(syncTags);
    }
    return releasedVersion;
  }

  public static String getNumbersOnly(String version) {
    return StringUtils.defaultIfBlank(version, StringUtils.EMPTY).split(CommonConstants.DASH_SEPARATOR)[0];
  }

  public static boolean isMajorVersion(String version) {
    return getNumbersOnly(version).split(MAIN_VERSION_REGEX).length == 1 && isReleasedVersion(version);
  }

  public static boolean isMinorVersion(String version) {
    return getNumbersOnly(version).split(MAIN_VERSION_REGEX).length == 2 && isReleasedVersion(version);
  }

  public static List<String> getInstallableVersionsFromMetadataList(List<Metadata> metadataList) {
    List<String> installableVersions = new ArrayList<>();
    if (CollectionUtils.isEmpty(metadataList)) {
      return installableVersions;
    }
    metadataList.stream().filter(
        metadata -> MavenUtils.isProductMetadata(metadata) && ObjectUtils.isNotEmpty(metadata.getVersions())).forEach(
        productMeta -> installableVersions.addAll(productMeta.getVersions()));
    return installableVersions.stream().distinct().sorted(new LatestVersionComparator()).toList();
  }

  public static String getPrefixOfVersion(String version) {
    return version.substring(0, version.indexOf(DOT_SEPARATOR));
  }

  public static List<String> extractAllVersions(List<MavenArtifactVersion> existingMavenArtifactVersion,
      boolean isShowDevVersion, String designerVersion) {
    Set<String> versions = existingMavenArtifactVersion.stream()
        .map(MavenArtifactVersion::getId)
        .map(MavenArtifactKey::getProductVersion)
        .collect(Collectors.toSet());

    return getVersionsToDisplay(new ArrayList<>(versions), isShowDevVersion,
        designerVersion);
  }

}
