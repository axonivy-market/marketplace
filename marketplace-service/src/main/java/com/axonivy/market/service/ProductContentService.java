package com.axonivy.market.service;

import com.axonivy.market.bo.VersionDownload;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ProductModuleContent;

public interface ProductContentService {
  ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName);

  VersionDownload downloadZipArtifactFile(String productId, String artifactId, String version);
}
