package com.axonivy.market.service;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;

public interface VersionService {

  List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactId);

  String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactId);

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  Map<String, Object> getProductJsonContentByIdAndVersion(String name , String version)
      throws JsonProcessingException;

  List<String> getVersionsForDesigner(String productId);
}