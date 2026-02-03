package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.factory.CoreVersionFactory;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.service.impl.CoreVersionServiceImpl;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.stable.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
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

  @Override
  public List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions,
      Boolean isShowDevVersion, String designerVersion) {
    List<String> result = new ArrayList<>();
    List<String> installableVersions = CoreVersionUtils.extractAllVersions(mavenArtifactVersions, isShowDevVersion);
    if (StringUtils.isBlank(designerVersion)) {
      result.add(CollectionUtils.firstElement(installableVersions));
      return result;
    }
    String version = CoreVersionFactory.findVersionStartWithOrNull(installableVersions, designerVersion);
    if (StringUtils.isNotBlank(version)) {
      result.add(version);
      return result;
    }
    version = CoreVersionFactory.findLowerVersion(installableVersions, designerVersion);
    if (StringUtils.isNotBlank(version)) {
      result.add(version);
    }
    return result;
  }
}
