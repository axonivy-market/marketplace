package com.axonivy.market.service;

import com.axonivy.market.entity.Artifact;

import java.util.List;

public interface MetadataService {
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);
}
