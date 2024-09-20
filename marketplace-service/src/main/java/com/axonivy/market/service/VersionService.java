package com.axonivy.market.service;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import java.util.List;
import java.util.Map;

public interface VersionService {

  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  Map<String, Object> getProductJsonContentByIdAndVersion(String name , String version);

  List<VersionAndUrlModel> getVersionsForDesigner(String productId);
}