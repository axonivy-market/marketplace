package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.repository.CoreGithubRepoRepository;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.CoreProductMarketplaceDataService;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.core.service.impl.CoreProductServiceImpl;
import com.axonivy.market.stable.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@Primary
public class ProductServiceImpl extends CoreProductServiceImpl implements ProductService {
  public ProductServiceImpl(CoreProductRepository coreProductRepo, CoreMetadataRepository coreMetadataRepo,
      CoreProductMarketplaceDataService coreProductMarketplaceDataService,
      CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepository,
      CoreProductJsonContentRepository coreProductJsonContentRepo, CoreGithubRepoRepository coreGithubRepoRepository,
      CoreVersionService coreVersionService) {
    super(coreProductRepo, coreMetadataRepo, coreProductMarketplaceDataService, coreMavenArtifactVersionRepository,
        coreProductJsonContentRepo, coreGithubRepoRepository, coreVersionService);
  }
}
