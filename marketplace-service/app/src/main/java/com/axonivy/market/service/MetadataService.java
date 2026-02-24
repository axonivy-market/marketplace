package com.axonivy.market.service;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.Metadata;
import org.apache.maven.model.Dependency;

import java.util.List;

public interface MetadataService {
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);

  Metadata getMetadataByVersion(Dependency dependencyModel, String dependencyVersion);
}
