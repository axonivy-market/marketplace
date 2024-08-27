package com.axonivy.market.service;

import com.axonivy.market.entity.productjsonfilecontent.ProductJsonContent;
import com.axonivy.market.model.MavenArtifactVersionModel;

import java.util.List;

public interface VersionService {

  List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactId);

  String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactId);

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  ProductJsonContent getProductJsonContentFromNameAndVersion(String name , String version);

  List<String> getVersionsForDesigner(String productId, Boolean isShowDevVersion, String designerVersion);
}