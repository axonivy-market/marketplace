package com.axonivy.market.service;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ProductModuleContent;

import java.io.OutputStream;
import java.util.List;

public interface ProductContentService {
  ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName);

  List<String> getDependencyUrls(String productId, String artifactId, String version);

  void buildArtifactZipStreamFromUrls(String productId, List<String> urls, OutputStream out);
}
