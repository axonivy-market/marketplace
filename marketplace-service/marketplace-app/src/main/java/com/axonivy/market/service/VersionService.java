package com.axonivy.market.service;

import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.model.VersionAndUrlModel;

import java.util.List;
import java.util.Map;

public interface VersionService extends CoreVersionService {

  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String version, String designerVersion);

  List<VersionAndUrlModel> getInstallableVersions(String productId, Boolean isShowDevVersion, String designerVersion);

  String getLatestVersionArtifactDownloadUrl(String productId, String version, String artifact);
}
