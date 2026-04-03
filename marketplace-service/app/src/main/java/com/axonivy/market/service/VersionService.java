package com.axonivy.market.service;

import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.model.VersionAndUrlModel;

import java.util.List;
import java.util.Map;

public interface VersionService extends CoreVersionService {

  /**
   * <p>
   * Get product json content by id and version
   * </p>
   *
   * @param  name
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @param  designerVersion
   *              type {@link String}
   * @return {@link Map<String, Object>}
   * @author nntthuy
   */
  Map<String, Object> getProductJsonContentByIdAndVersion(String name, String version, String designerVersion);

  /**
   * <p>
   * Get installable product metadata versions
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  isShowDevVersion
   *              type {@link Boolean}
   * @param  designerVersion
   *              type {@link String}
   * @return {@link List<VersionAndUrlModel>}
   * @author ntqdinh
   */
  List<VersionAndUrlModel> getInstallableVersions(String productId, Boolean isShowDevVersion, String designerVersion);

  /**
   * <p>
   * Get latest maven version artifact download url
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @param  artifact
   *              type {@link String}
   * @return {@link }
   * @author ntqdinh
   */
  String getLatestVersionArtifactDownloadUrl(String productId, String version, String artifact);
}
