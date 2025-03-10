package com.axonivy.market.service;

import com.axonivy.market.bo.Artifact;

import java.util.List;

public interface MetadataService {
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);
}
