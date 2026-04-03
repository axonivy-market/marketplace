package com.axonivy.market.service;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.Metadata;
import org.apache.maven.model.Dependency;

import java.util.List;

public interface MetadataService {

  /**
   * <p>
   * Update artifact and meta data
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  versions
   *              type {@link List<String>}
   * @param  artifacts
   *              type {@link List<Artifact>}
   * @return {@link }
   * @author tvtphuc
   */
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);

  /**
   * <p>
   * Get metadata by version
   * </p>
   *
   * @param  dependencyModel
   *              type {@link Dependency}
   * @param  dependencyVersion
   *              type {@link String}
   * @return {@link Metadata}
   * @author ttan
   */
  Metadata getMetadataByVersion(Dependency dependencyModel, String dependencyVersion);
}
