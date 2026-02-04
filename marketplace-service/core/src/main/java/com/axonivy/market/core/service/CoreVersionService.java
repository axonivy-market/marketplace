package com.axonivy.market.core.service;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.model.MavenArtifactVersionModel;

import java.util.List;
import java.util.Map;

public interface CoreVersionService {
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String designerVersion);

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions, Boolean isShowDevVersion,
      String designerVersion);

  String getLatestInstallableVersion(String productId);
}
