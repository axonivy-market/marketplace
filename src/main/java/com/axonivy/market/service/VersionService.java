package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface VersionService {
    boolean isReleasedVersionOrUnReleaseDevVersion(List<String> versions, String version);

    boolean isSnapshotVersion(String version);

    boolean isSprintVersion(String version);

    boolean isReleasedVersion(String version);

    List<String> getVersionsFromArtifactInfo(String repoUrl, String groupId, String artifactID);

    //TODO: need to rework this method
    List<String> getVersionsToDisplay(String productId, Boolean isShowDevVersion, String designerVersion) throws IOException;

    List<String> getVersionsFromProduct(Product product, Boolean isShowDevVersion, String designerVersion);

    String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID);

    Map<String, List<String>> getArtifactsToDisplay(String productId) throws IOException;
}
