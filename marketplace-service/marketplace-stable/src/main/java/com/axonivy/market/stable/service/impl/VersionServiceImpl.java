package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.service.impl.CoreVersionServiceImpl;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.stable.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Service
@Log4j2
public class VersionServiceImpl extends CoreVersionServiceImpl implements VersionService {

  public VersionServiceImpl(CoreProductJsonContentRepository coreProductJsonRepo,
      CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepo,
      CoreMetadataRepository coreMetadataRepository) {
    super(coreProductJsonRepo, coreMavenArtifactVersionRepo, coreMetadataRepository);
  }

  @Override
  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String designerVersion) {

    Map<String, Object> result = super.getProductJsonContentByIdAndVersion(productId, designerVersion);
    if (CollectionUtils.isEmpty(result) && CoreVersionUtils.isReleasedVersion(designerVersion)) {
      result = super.getProductJsonContentByIdAndVersion(productId, designerVersion + "-SNAPSHOT");
    }
    return result;
  }
}
