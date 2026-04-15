package com.axonivy.market.service;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.Metadata;
import org.apache.maven.model.Dependency;

import java.util.List;

public interface MetadataService {

  /**
   * <p>
   * Updates or creates artifact and metadata records for a product. Synchronizes Maven artifact information
   * with specific product versions, enabling version-to-artifact mapping for downloads and installations.
   * Creates or updates Metadata entities that link product versions to their corresponding Maven artifacts.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to update artifacts for
   * @param  versions
   *              type {@link List<String>} - list of product versions to create or update metadata for
   * @param  artifacts
   *              type {@link List<Artifact>} - list of Maven artifacts associated with these product versions
   * @return void - no return value; updates are persisted directly to the artifact and metadata repositories
   * @author tvtphuc
   */
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);

  /**
   * <p>
   * Retrieves or creates metadata for a specific dependency version. Extracts metadata information from
   * Maven POM file for the requested version, including group ID, artifact ID, and version information.
   * Used to populate product artifact metadata during synchronization.
   * </p>
   *
   * @param  dependencyModel
   *              type {@link Dependency} - the Maven dependency model containing groupId and artifactId
   * @param  dependencyVersion
   *              type {@link String} - the specific dependency version to fetch metadata for
   * @return {@link Metadata} - metadata object containing dependency information (group ID, artifact ID, version);
   *         returns null if metadata cannot be resolved for the specified version
   * @author ttan
   */
  Metadata getMetadataByVersion(Dependency dependencyModel, String dependencyVersion);
}
