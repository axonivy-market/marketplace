package com.axonivy.market.service;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ProductModuleContent;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface ProductContentService {
  ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName);

  List<String> getDependencyUrls(String productId, String artifactId, String version);

  StreamingResponseBody buildArtifactStreamUrls(List<String> urls);
}
