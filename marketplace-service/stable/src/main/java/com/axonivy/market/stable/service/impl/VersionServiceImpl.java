package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.comparator.LatestVersionComparator;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.service.impl.CoreVersionServiceImpl;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.stable.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class VersionServiceImpl extends CoreVersionServiceImpl implements VersionService {
  public VersionServiceImpl(CoreProductJsonContentRepository coreProductJsonRepo,
      CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepo,
      CoreMetadataRepository coreMetadataRepository) {
    super(coreProductJsonRepo, coreMavenArtifactVersionRepo, coreMetadataRepository);
  }

  @Override
  public List<String> getMavenVersionsToDisplay(List<MavenArtifactVersion> mavenArtifactVersions,
      Boolean isShowDevVersion, String designerVersion) {
    List<String> result = CoreVersionUtils.extractAllVersions(mavenArtifactVersions, isShowDevVersion);
    if (StringUtils.isBlank(designerVersion)) {
      return result;
    }
    return result.stream().dropWhile(v -> new LatestVersionComparator().compare(v, designerVersion) < 0).toList();
  }
}
