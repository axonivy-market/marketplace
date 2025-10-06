package com.axonivy.market.factory;

import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.enums.DevelopmentVersion;
import com.axonivy.market.util.VersionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MavenConstants.DEV_RELEASE_POSTFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionFactory {
  static final String PROJECT_VERSION = "${project.version}";
  // Maven range version pattern, for example: [1.0, 2.0] or (1.0, 2.0) or [1.0, 2.0) or (1.0, 2.0]
  static final String RANGE_VERSION_PATTERN = "[\\[\\]()]";
  // The arrays of all operators can appear in maven range version format
  static final String[] MAVEN_RANGE_VERSION_ARRAYS = new String[] {"(","]","[",")"};

  public static String resolveVersion(String mavenVersion, String defaultVersion) {
    if (StringUtils.equalsIgnoreCase(PROJECT_VERSION, mavenVersion)) {
      return defaultVersion;
    }
    if (StringUtils.containsAnyIgnoreCase(mavenVersion, MAVEN_RANGE_VERSION_ARRAYS)) {
      var plainVersions = mavenVersion.replaceAll(RANGE_VERSION_PATTERN, EMPTY);
      String[] parts = plainVersions.split(CommonConstants.COMMA);
      return parts.length > 1 ? parts[1].trim() : parts[0].trim();
    }
    return defaultVersion;
  }

  public static String get(List<String> versions, String requestedVersion) {
    var sortedVersions = Optional.ofNullable(versions).orElse(new ArrayList<>()).stream()
        .filter(Objects::nonNull)
        .sorted((v1, v2) -> MavenVersionComparator.compare(v2, v1)).toList();
    // Redirect to the newest version for special keywords
    var version = DevelopmentVersion.of(requestedVersion);

    // Get latest released version if requested version is 'latest'
    if (version == DevelopmentVersion.LATEST) {
      return sortedVersions.stream().filter(VersionUtils::isReleasedVersion)
              .findFirst().orElse(null);
    }

    if (version != null && !sortedVersions.isEmpty()) {
      return sortedVersions.get(0);
    }

    // e.g. 10.0-dev
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
    }
    return findVersionStartWith(sortedVersions, requestedVersion);
  }

  public static String getBestMatchMajorVersion(List<String> versions, String requestedVersion
          , List<String> majorVersions) {
    String bestMatchVersion = get(versions, requestedVersion);
    Map<String, String> latestSupportedDocVersions = getMapMajorVersionToLatestVersion(versions, majorVersions);
    return latestSupportedDocVersions.getOrDefault(bestMatchVersion, bestMatchVersion);
  }

  public static Map<String, String> getMapMajorVersionToLatestVersion(List<String> versions, List<String> majorVersions) {
    return majorVersions.stream().map(v -> Map.entry(VersionFactory.get(versions, v), v))
        .filter(e -> e.getKey() != null && !e.getKey().isEmpty())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (a, b) -> a,
            LinkedHashMap::new
        ));
  }

  public static String getFromMetadata(List<Metadata> metadataList, String requestedVersion) {
    var version = DevelopmentVersion.of(requestedVersion);

    // Get latest dev version from metadata
    if (Objects.nonNull(version) && version != DevelopmentVersion.LATEST) {
      return metadataList.stream().map(Metadata::getLatest).min(new LatestVersionComparator()).orElse(EMPTY);
    }

    List<String> artifactVersions = metadataList.stream().flatMap(metadata -> metadata.getVersions().stream()).sorted(
        new LatestVersionComparator()).toList();
    List<String> releasedVersions = artifactVersions.stream().filter(VersionUtils::isReleasedVersion).sorted(
        new LatestVersionComparator()).toList();

    // Get latest released version from metadata
    if (version == DevelopmentVersion.LATEST) {
      return releasedVersions.stream().min(new LatestVersionComparator()).orElse(EMPTY);
    }

    // Get latest dev version from specific version
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
      return findVersionStartWith(artifactVersions, requestedVersion);
    }

    String matchVersion = findVersionStartWith(releasedVersions, requestedVersion);

    // Return latest version of specific version if can not fnd latest release of that version
    if ((VersionUtils.isMajorVersion(requestedVersion) || VersionUtils.isMinorVersion(
        requestedVersion)) && !CollectionUtils.containsInstance(releasedVersions, matchVersion)) {
      return findVersionStartWith(artifactVersions, requestedVersion);
    }
    return matchVersion;
  }

  private static String findVersionStartWith(List<String> releaseVersions, String version) {
    return CollectionUtils.isEmpty(releaseVersions) ? version : releaseVersions.stream().filter(
            ver -> ver.startsWith(version)).findAny().orElse(StringUtils.EMPTY);
  }
}
