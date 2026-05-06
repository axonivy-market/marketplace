package com.axonivy.market.factory;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.core.comparator.MavenVersionComparator;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.enums.DevelopmentVersion;
import com.axonivy.market.core.factory.CoreVersionFactory;
import com.axonivy.market.core.strategy.VersionMatchStrategy;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.strategy.impl.StartsWithVersionStrategy;
import com.axonivy.market.util.VersionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MavenConstants.DEV_RELEASE_POSTFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionFactory extends CoreVersionFactory {
  private static final String PROJECT_VERSION = "${project.version}";
  // Maven range version pattern, for example: [1.0, 2.0] or (1.0, 2.0) or [1.0, 2.0) or (1.0, 2.0]
  private static final Pattern RANGE_VERSION_PATTERN = Pattern.compile("[\\[\\]()]");
  // The arrays of all operators can appear in maven range version format
  private static final String[] MAVEN_RANGE_VERSION_ARRAYS = new String[]{"(", "]", "[", ")"};

  private static final VersionMatchStrategy DEFAULT_STRATEGY = new StartsWithVersionStrategy();
  private static final MavenVersionComparator VERSION_COMPARATOR = MavenVersionComparator.getInstance();

  public static String resolveVersion(String mavenVersion, String defaultVersion) {
    String resolvedVersion = defaultVersion;
    if (StringUtils.equalsIgnoreCase(PROJECT_VERSION, mavenVersion)) {
      return defaultVersion;
    } else if (StringUtils.containsAnyIgnoreCase(mavenVersion, MAVEN_RANGE_VERSION_ARRAYS)) {
      resolvedVersion = extractVersionFromRange(mavenVersion);
    } else if (StringUtils.isNotBlank(mavenVersion) && !StringUtils.equals(mavenVersion, defaultVersion)) {
      resolvedVersion = mavenVersion.trim();
    }

    return resolvedVersion;
  }

  private static String extractVersionFromRange(String mavenVersion) {
    var plainVersions = RANGE_VERSION_PATTERN.matcher(mavenVersion).replaceAll(EMPTY);
    String[] parts = plainVersions.split(CoreCommonConstants.COMMA);
    if (parts.length > CoreCommonConstants.ONE) {
      return parts[CoreCommonConstants.ONE].trim();
    }
    return parts[CommonConstants.ZERO].trim();
  }

  public static String get(List<String> versions, String requestedVersion) {
    return CoreVersionFactory.get(versions, requestedVersion, DEFAULT_STRATEGY);
  }

  public static String getBestMatchMajorVersion(List<String> versions, String requestedVersion) {
    return Optional.ofNullable(versions).stream()
        .flatMap(List::stream)
        .filter(Objects::nonNull)
        .sorted(VERSION_COMPARATOR)
        .filter(ver -> ver.startsWith(requestedVersion)).findFirst().orElse(EMPTY);
  }

  public static Map<String, String> getMapMajorVersionToLatestVersion(List<String> versions,
      List<String> majorVersions) {
    return majorVersions.stream().map(version -> Map.entry(version, VersionFactory.get(versions, version)))
        .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (a, b) -> a,
            LinkedHashMap::new
        ));
  }

  public static String getFromMetadata(Collection<Metadata> metadataList, String requestedVersion) {
    var version = DevelopmentVersion.of(requestedVersion);

    // Get latest dev version from metadata
    if (Objects.nonNull(version) && version != DevelopmentVersion.LATEST) {
      return metadataList.stream().map(Metadata::getLatest).min(VERSION_COMPARATOR).orElse(EMPTY);
    }

    List<String> artifactVersions = metadataList.stream().flatMap(metadata -> metadata.getVersions().stream()).sorted(
        VERSION_COMPARATOR).toList();
    List<String> releasedVersions = artifactVersions.stream().filter(CoreVersionUtils::isReleasedVersion).sorted(
        VERSION_COMPARATOR).toList();

    // Get latest released version from metadata
    if (version == DevelopmentVersion.LATEST) {
      return releasedVersions.stream().min(VERSION_COMPARATOR).orElse(EMPTY);
    }

    String result;
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      // Get latest dev version from specific version
      String latestDevVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
      result = findVersionStartWith(artifactVersions, latestDevVersion);
    } else {
      String matchVersion = findVersionStartWith(releasedVersions, requestedVersion);

      // Return latest version of specific version if can not find latest release of that version
      if ((VersionUtils.isMajorVersion(requestedVersion) || VersionUtils.isMinorVersion(requestedVersion))
          && !CollectionUtils.containsInstance(releasedVersions, matchVersion)) {
        result = findVersionStartWith(artifactVersions, requestedVersion);
      } else {
        result = matchVersion;
      }
    }

    return result;
  }

  private static String findVersionStartWith(List<String> releaseVersions, String version) {
    if (CollectionUtils.isEmpty(releaseVersions)) {
      return version;
    }
    return releaseVersions.stream().filter(ver -> ver.startsWith(version)).findAny().orElse(releaseVersions.get(0));
  }
}
