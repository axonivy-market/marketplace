package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.builder.ProductJsonLinkBuilder;
import com.axonivy.market.core.comparator.LatestVersionComparator;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.service.impl.CoreVersionServiceImpl;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.stable.repository.MavenArtifactVersionRepository;
import com.axonivy.market.stable.repository.MetadataRepository;
import com.axonivy.market.stable.repository.ProductJsonContentRepository;
import com.axonivy.market.stable.service.ProductMarketplaceDataService;
import com.axonivy.market.stable.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
@Primary
public class VersionServiceImpl extends CoreVersionServiceImpl implements VersionService {
  private final ProductMarketplaceDataService productMarketplaceDataService;

  public VersionServiceImpl(
      ProductJsonLinkBuilder productJsonLinkBuilder, ProductJsonContentRepository productJsonRepo,
      ProductMarketplaceDataService productMarketplaceDataService, MetadataRepository metadataRepo,
      MavenArtifactVersionRepository mavenArtifactVersionRepo) {
    super(productJsonRepo, mavenArtifactVersionRepo, metadataRepo, productJsonLinkBuilder);
    this.productMarketplaceDataService = productMarketplaceDataService;
  }

  @Override
  public Map<String, Object> getProductJsonContentByIdAndVersion(String productId, String version,
      String designerVersion) {
    Map<String, Object> result = getProductJsonContentByIdAndVersion(productId, version);

    if (!CollectionUtils.isEmpty(result)) {
      productMarketplaceDataService.updateInstallationCountForProduct(productId, designerVersion);
    }
    return result;
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
