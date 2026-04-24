package com.axonivy.market.service;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.Metadata;
import org.apache.maven.model.Dependency;

import java.util.List;

public interface MetadataService {

  /**
   * <p>
   * Updates artifact and metadata records for a product. Retrieves existing metadata for the product,
   * extracts Maven artifacts from ProductJsonContent for specified versions, and combines with provided
   * artifacts. Downloads Maven metadata XML files to populate version information, updates MavenArtifactVersion
   * entities for version-to-artifact mapping, and persists all metadata changes to enable downloads and installations.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to update artifacts for
   * @param  versions
   *              type {@link List<String>} - list of product versions to extract artifacts from ProductJsonContent;
   *              can be null or empty if only direct artifacts are provided
   * @param  artifacts
   *              type {@link List<Artifact>} - list of Maven artifacts to directly include; can be null or empty
   *              if only version-based artifacts are processed
   * @author tvtphuc
   */
  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);

  /**
   * <p>
   * Retrieves or creates metadata for a specific dependency version. First checks if existing metadata
   * for the dependency already contains the requested version. If found, returns the existing metadata.
   * Otherwise, retrieves existing metadata or creates new metadata, downloads Maven metadata XML files
   * to populate version information, and returns the updated metadata containing the requested version.
   * Used to populate product artifact metadata during synchronization.
   * </p>
   *
   * @param  dependencyModel
   *              type {@link Dependency} - the Maven dependency model containing groupId and artifactId
   * @param  dependencyVersion
   *              type {@link String} - the specific dependency version to fetch metadata for
   * @return {@link Metadata} - metadata object containing dependency information (group ID, artifact ID, version);
   *         returns existing metadata if version already exists, or updated/created metadata with the version;
   *         returns original metadata if version update fails
   * @author ttan
   */
  Metadata getMetadataByVersion(Dependency dependencyModel, String dependencyVersion);
}
