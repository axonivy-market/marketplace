package com.axonivy.market.service;

import java.io.IOException;
import java.util.List;

public interface VersionService {

    List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactID);

    List<String> getVersionsToDisplay(String productId, Boolean isShowDevVersion, String designerVersion) throws IOException;

    String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID);

//    Map<String, List<String>> getArtifactsToDisplay(String productId) throws IOException;
}
