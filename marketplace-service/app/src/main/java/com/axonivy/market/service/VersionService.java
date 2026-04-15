package com.axonivy.market.service;

import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.model.VersionAndUrlModel;

import java.util.List;
import java.util.Map;

public interface VersionService extends CoreVersionService {

  /**
   * <p>
   * Retrieves product JSON configuration and metadata for a specific version and Designer version combination.
   * Returns the complete product configuration, features, dependencies, and installation instructions in
   * a structured JSON format ready for Designer UI consumption.
   * </p>
   *
   * @param  name
   *              type {@link String} - the product name/ID to retrieve JSON content for
   * @param  version
   *              type {@link String} - the specific product version
   * @param  designerVersion
   *              type {@link String} - the AxonIvy Designer version for which to retrieve compatible configuration
   * @return {@link Map<String, Object>} - product JSON content as a key-value map containing metadata, features,
   *         dependencies, and configuration; returns null if product/version not found
   * @author nntthuy
   */
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String version, String designerVersion);

  /**
   * <p>
   * Retrieves all installable versions of a product for a specific Designer version. Returns compatible
   * versions with their download URLs and metadata, optionally including development versions (dev, nightly).
   * Filters versions to show only those compatible with the requested Designer version.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  isShowDevVersion
   *              type {@link Boolean} - if true, includes development versions; if false, only released versions;
   *              null defaults to false
   * @param  designerVersion
   *              type {@link String} - the AxonIvy Designer version to filter compatible product versions
   * @return {@link List<VersionAndUrlModel>} - list of installable versions with download URLs and metadata;
   *         returns empty list if no compatible versions found
   * @author ntqdinh
   */
  List<VersionAndUrlModel> getInstallableVersions(String productId, Boolean isShowDevVersion, String designerVersion);

  /**
   * <p>
   * Retrieves the Maven repository download URL for the latest version of a specific product artifact.
   * Constructs the full URL based on Maven coordinates (groupId, artifactId, version) for direct artifact
   * download from Maven Central or configured Maven mirror.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  version
   *              type {@link String} - the specific product version to get artifact for
   * @param  artifact
   *              type {@link String} - the artifact ID or name within the product
   * @return {@link String} - the complete Maven artifact download URL; returns null if product, version,
   *         or artifact not found
   * @author ntqdinh
   */
  String getLatestVersionArtifactDownloadUrl(String productId, String version, String artifact);
}
