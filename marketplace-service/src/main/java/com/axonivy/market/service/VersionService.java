package com.axonivy.market.service;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;

import java.util.List;
import java.util.Map;

public interface VersionService {

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String version, String designerVersion);

  List<VersionAndUrlModel> getVersionsForDesigner(String productId, Boolean isShowDevVersion, String designerVersion);

  String getLatestVersionArtifactDownloadUrl(String productId, String version, String artifact);
}