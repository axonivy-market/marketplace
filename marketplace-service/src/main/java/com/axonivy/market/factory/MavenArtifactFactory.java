package com.axonivy.market.factory;

import com.axonivy.market.comparator.MavenVersionComparator;

import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL;
import static com.axonivy.market.constants.MavenConstants.ARTIFACT_FILE_NAME_FORMAT;
import static com.axonivy.market.constants.CommonConstants.DOT_SEPARATOR;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenArtifactFactory {

  /**
   * Build a maven repo download URL from MavenArtifact
   * Format repo/groupId/artifactId/version/artifact
   * @param artifact is {@link MavenArtifact}
   * @param version is request version
   * @return maven repo download url
   */
  public static String buildDownloadUrlByVersion(MavenArtifact artifact, String version) {
    String groupIdByVersion = artifact.getGroupId();
    String artifactIdByVersion = artifact.getArtifactId();
    String repoUrl = StringUtils.defaultIfBlank(artifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);

    ArchivedArtifact archivedArtifactBestMatchVersion = findArchivedArtifactBestMatchVersion(version,
        artifact.getArchivedArtifacts());
    if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
      groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
      artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
    }
    groupIdByVersion = groupIdByVersion.replace(DOT_SEPARATOR, SLASH);

    String artifactFileName = String.format(ARTIFACT_FILE_NAME_FORMAT, artifactIdByVersion, version,
        artifact.getType());
    return String.join(SLASH, repoUrl, groupIdByVersion, artifactIdByVersion, version, artifactFileName);
  }

  public static ArchivedArtifact findArchivedArtifactBestMatchVersion(String version,
      List<ArchivedArtifact> archivedArtifacts) {
    if (CollectionUtils.isEmpty(archivedArtifacts)) {
      return null;
    }
    return archivedArtifacts.stream().filter(
        archivedArtifact -> MavenVersionComparator.compare(archivedArtifact.getLastVersion(),
            version) >= 0).findAny().orElse(null);
  }
}
