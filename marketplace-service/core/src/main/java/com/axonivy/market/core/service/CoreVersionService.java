package com.axonivy.market.core.service;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.model.VersionAndUrlModel;

import java.util.List;
import java.util.Map;

public interface CoreVersionService {
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String productVersion);

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions, Boolean isShowDevVersion,
      String designerVersion);

  String getLatestReleasedVersion(String productId);

  List<VersionAndUrlModel> getInstallableVersions(String productId, Boolean isShowDevVersion, String designerVersion);
}
