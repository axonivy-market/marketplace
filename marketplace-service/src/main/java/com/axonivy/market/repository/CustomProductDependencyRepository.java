package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.ProductDependency;

import java.util.List;

public interface CustomProductDependencyRepository {
  List<ProductDependency> findProductDependencies(String productId, String artifactId, String version);

  List<MavenArtifactVersion> findMavenArtifactVersions(String productId, String artifactId, String version);
}
