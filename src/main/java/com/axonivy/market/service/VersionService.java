package com.axonivy.market.service;

import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.model.MavenArtifactModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface VersionService {

    List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactID);

    List<String> getVersionsToDisplay(List<MavenArtifact> artifacts, Boolean isShowDevVersion, String designerVersion) throws IOException;

    String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID);

    Map<String, List<MavenArtifactModel>> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion, String designerVersion);
}
