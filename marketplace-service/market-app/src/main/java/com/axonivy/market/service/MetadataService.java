package com.axonivy.market.service;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.Metadata;
import org.apache.maven.model.Dependency;

import java.util.List;

public interface MetadataService {
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);

  Metadata getMetadataByVersion(Dependency dependencyModel, String dependencyVersion);
}
