package com.axonivy.market.core.utils;

import com.axonivy.market.core.comparator.LatestVersionComparator;
import com.axonivy.market.core.comparator.MavenVersionComparator;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.core.constants.CoreCommonConstants.*;
import static com.axonivy.market.core.constants.CoreMavenConstants.*;

@Log4j2
@NoArgsConstructor
public class CoreVersionUtils {
  private static final Pattern MAIN_VERSION_PATTERN = Pattern.compile(MAIN_VERSION_REGEX);
  private static final Pattern SPRINT_RELEASE_PATTERN = Pattern.compile(SPRINT_RELEASE_POSTFIX);

  public static List<String> extractAllVersions(Collection<MavenArtifactVersion> existingMavenArtifactVersion,
      boolean isShowDevVersion) {
    Set<String> versions = existingMavenArtifactVersion.stream().map(MavenArtifactVersion::getId).map(
        MavenArtifactKey::getProductVersion).collect(Collectors.toSet());

    return getVersionsToDisplay(new ArrayList<>(versions), isShowDevVersion);
  }

  public static List<String> getVersionsToDisplay(List<String> versions, Boolean isShowDevVersion) {
    Stream<String> versionStream = versions.stream();
    if (BooleanUtils.isTrue(isShowDevVersion)) {
      return versionStream.filter(version -> isOfficialVersionOrUnReleasedDevVersion(versions, version)).sorted(
          new LatestVersionComparator()).toList();
    }
    return versions.stream().filter(CoreVersionUtils::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
  }

  public static boolean isReleasedVersion(String version) {
    return !(isSprintVersion(version) || isSnapshotVersion(version)) && isValidFormatReleasedVersion(version);
  }

  public static boolean isSnapshotVersion(String version) {
    return version.endsWith(SNAPSHOT_RELEASE_POSTFIX);
  }

  public static boolean isSprintVersion(String version) {
    return version.contains(SPRINT_RELEASE_POSTFIX);
  }

  public static boolean isValidFormatReleasedVersion(String version) {
    return StringUtils.isNumeric(MAIN_VERSION_PATTERN.split(version)[0]);
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
      bugfixVersion = getBugfixVersion(SPRINT_RELEASE_PATTERN.split(version)[0]);
    }
    return versions.stream().noneMatch(
        currentVersion -> !currentVersion.equals(version) && isReleasedVersion(currentVersion) && getBugfixVersion(
            currentVersion).equals(bugfixVersion));
  }

  public static String getBugfixVersion(String version) {
    if (isSnapshotVersion(version)) {
      version = version.replace(SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY);
    } else if (isSprintVersion(version)) {
      version = SPRINT_RELEASE_PATTERN.split(version)[0];
    }
    String[] segments = MAIN_VERSION_PATTERN.split(version);
    if (segments.length >= THREE) {
      segments[TWO] = segments[TWO].split(CoreCommonConstants.DASH_SEPARATOR)[0];
      return segments[0] + CoreCommonConstants.DOT_SEPARATOR + segments[ONE] + CoreCommonConstants.DOT_SEPARATOR + segments[TWO];
    }
    return version;
  }

  public static String getBestMatchVersion(List<String> versions, String designerVersion) {
    return getBestMatchVersion(versions, designerVersion, true);
  }

    public static String getBestMatchVersion(List<String> versions, String designerVersion, Boolean allowDevVersion) {
    if (CollectionUtils.isEmpty(versions)) {
      return null;
    }
    // Filter matching version first
    String bestMatchVersion = versions.stream().filter(
        version -> StringUtils.equals(version, designerVersion)).findAny().orElse(null);
    //Next priority: prior released version
    if (StringUtils.isBlank(bestMatchVersion)) {
      bestMatchVersion = versions.stream().filter(
          version -> MavenVersionComparator.compare(version, designerVersion) < 0 && isReleasedVersion(
              version)).findAny().orElse(null);
    }
    //Next priority: prior dev version
    if (StringUtils.isBlank(bestMatchVersion) && allowDevVersion) {
      bestMatchVersion = versions.stream().filter(
          version -> MavenVersionComparator.compare(version, designerVersion) < 0).findAny().orElse(null);
    }
    //Next priority: any prior release version
    if (StringUtils.isBlank(bestMatchVersion)) {
      bestMatchVersion = versions.stream().filter(CoreVersionUtils::isReleasedVersion).findAny().orElse(
          CollectionUtils.firstElement(versions));
    }
    return bestMatchVersion;
  }
}
