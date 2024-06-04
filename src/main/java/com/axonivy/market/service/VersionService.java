package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

import java.util.List;

public interface VersionService {
    boolean isReleasedVersionOrUnReleaseDevVersion(List<String> versions, String version);

    boolean isSnapshotVersion(String version);

    boolean isSprintVersion(String version);

    boolean isReleasedVersion(String version);

    List<String> getVersionsFromArtifactInfo(String repoUrl, String groupId, String artifactID);

    List<String> getVersionsFromMaven(Product product, Boolean isShowDevVersion, String designerVersion);

    String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID);

    List<String> getVersionsToDisplay(String productId, boolean isShowDevVersion, String designerVersion);
}
