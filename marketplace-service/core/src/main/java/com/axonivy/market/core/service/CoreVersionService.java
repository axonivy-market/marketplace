package com.axonivy.market.core.service;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.model.MavenArtifactVersionModel;

import java.util.List;
import java.util.Map;

public interface CoreVersionService {

  /**
   * <p>
   * Retrieves product JSON configuration and metadata for a specific version. Returns the complete
   * product configuration, features, dependencies, and installation instructions in a structured JSON
   * format suitable for marketplace display and API consumption.
   * </p>
   *
   * @param  name
   *              type {@link String} - the product name/ID to retrieve JSON content for
   * @param  productVersion
   *              type {@link String} - the specific product version to retrieve configuration for
   * @return {@link Map<String, Object>} - product JSON content as a key-value map containing metadata,
   *         features, dependencies, and configuration; returns empty map if product/version not found or JSON
   *         content cannot be parsed
   * @author ntqdinh
   */
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String productVersion);

  /**
   * <p>
   * Retrieves all artifacts and versions available for a product with filtering by Designer version
   * compatibility and development version visibility. Returns models formatted for display in marketplace UI
   * with version information, artifact details, and availability status.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to retrieve artifacts and versions for
   * @param  isShowDevVersion
   *              type {@link Boolean} - if true, includes development versions (dev, nightly); if false,
   *              only shows released versions; null defaults to false
   * @param  designerVersion
   *              type {@link String} - the AxonIvy Designer version to filter compatible artifacts/versions
   * @return {@link List<MavenArtifactVersionModel>} - list of artifact models with version information,
   *         download URLs, and compatibility details; returns empty list if no compatible artifacts found
   * @author ntqdinh
   */
  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  /**
   * <p>
   * Filters Maven artifact versions for display based on development version visibility and Designer
   * version compatibility. Applies business rules to show only relevant, compatible versions in the UI.
   * </p>
   *
   * @param  mavenArtifactVersions
   *              type {@link List<MavenArtifactVersion>} - list of all available Maven artifact versions
   * @param  isShowDevVersion
   *              type {@link Boolean} - if true, includes development versions; if false, only released versions;
   *              null defaults to false
   * @param  designerVersion
   *              type {@link String} - the AxonIvy Designer version to filter compatible versions
   * @return {@link List<String>} - list of version strings suitable for display, filtered and sorted in
   *         descending order; returns empty list if no compatible versions found
   * @author ntqdinh
   */
  List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions, Boolean isShowDevVersion,
      String designerVersion);

  /**
   * <p>
   * Retrieves the latest released (non-development) version of a product. Returns the highest version
   * number that has been officially released, excluding development versions (dev, nightly, snapshots).
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to find the latest released version for
   * @return {@link String} - the latest released version string in semantic versioning format;
   *         returns null if product not found or has no released versions
   * @author ntqdinh
   */
  String getLatestReleasedVersion(String productId);
}
