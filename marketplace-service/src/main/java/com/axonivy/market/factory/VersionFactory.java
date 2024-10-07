package com.axonivy.market.factory;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.enums.DevelopmentVersion;
import com.axonivy.market.util.VersionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MavenConstants.DEV_RELEASE_POSTFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionFactory {

  public static String get(List<String> versions, String requestedVersion) {
    var sortedVersions = Optional.ofNullable(versions).orElse(new ArrayList<>()).stream()
        .filter(Objects::nonNull)
        .sorted((v1, v2) -> MavenVersionComparator.compare(v2, v1)).toList();
    // Redirect to the newest version for special keywords
    var version = DevelopmentVersion.of(requestedVersion);
    if (version != null) {
      return sortedVersions.get(0);
    }

    // e.g. 10.0-dev
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
    }
    return findVersionStartWith(sortedVersions, requestedVersion);
  }

  public static String getFromMetadata(List<Metadata> metadataList, String requestedVersion) {
    var version = DevelopmentVersion.of(requestedVersion);

    // Get latest released version from metadata
    if (version == DevelopmentVersion.LATEST) {
      return metadataList.stream().map(Metadata::getRelease).max(new LatestVersionComparator()).orElse(
          EMPTY);
    }

    //Get latest dev version from metadata
    if (Objects.nonNull(version)) {
      return metadataList.stream().map(Metadata::getLatest).sorted(new LatestVersionComparator()).findFirst().orElse(
          EMPTY);
    }

    List<String> versionsInArtifact = metadataList.stream().flatMap(metadata -> metadata.getVersions().stream()).sorted(
        new LatestVersionComparator()).toList();

    //Get latest dev version from specific version
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
      return findVersionStartWith(versionsInArtifact, requestedVersion);
    }

    List<String> releasedVersions = versionsInArtifact.stream().filter(VersionUtils::isReleasedVersion).toList();
    String matchVersion = findVersionStartWith(releasedVersions, requestedVersion);

    // Return latest version of specific version if can not fnd latest release of that version
    if ((VersionUtils.isMajorVersion(requestedVersion) || VersionUtils.isMinorVersion(
        requestedVersion)) && !releasedVersions.contains(matchVersion)) {
      return findVersionStartWith(versionsInArtifact, requestedVersion);
    }
    return matchVersion;
  }
  private static String findVersionStartWith(List<String> releaseVersions, String version) {
    return Optional.ofNullable(releaseVersions).orElse(List.of()).stream().filter(
        ver -> ver.startsWith(version)).findAny().orElse(version);
  }
}
