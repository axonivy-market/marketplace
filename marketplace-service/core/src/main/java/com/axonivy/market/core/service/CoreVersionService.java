package com.axonivy.market.core.service;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.model.MavenArtifactVersionModel;

import java.util.List;
import java.util.Map;

public interface CoreVersionService {

  /**
   * <p>
   * Get product json content by id and version
   * </p>
   *
   * @param  name
   *              type {@link String}
   * @param  productVersion
   *              type {@link String}
   * @return {@link Map<String, Object>}
   * @author ntqdinh
   */
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String productVersion);

  /**
   * <p>
   * Get artifacts and version to display
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  isShowDevVersion
   *              type {@link Boolean}
   * @param  designerVersion
   *              type {@link String}
   * @return {@link List<MavenArtifactVersionModel>}
   * @author ntqdinh
   */
  List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion);

  /**
   * <p>
   * Get maven versions to display
   * </p>
   *
   * @param  mavenArtifactVersions
   *              type {@link List<MavenArtifactVersion>}
   * @param  isShowDevVersion
   *              type {@link Boolean}
   * @param  designerVersion
   *              type {@link String}
   * @return {@link List<String>}
   * @author ntqdinh
   */
  List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions, Boolean isShowDevVersion,
      String designerVersion);

  /**
   * <p>
   * Get latest released version by product id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @return {@link String}
   * @author ntqdinh
   */
  String getLatestReleasedVersion(String productId);
}
