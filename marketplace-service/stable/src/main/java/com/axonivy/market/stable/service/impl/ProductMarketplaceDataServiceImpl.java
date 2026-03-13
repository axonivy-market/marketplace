package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.repository.CoreGithubRepoRepository;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductDesignerInstallationRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.CoreProductMarketplaceDataService;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.core.service.impl.CoreProductMarketplaceDataServiceImpl;
import com.axonivy.market.core.service.impl.CoreProductServiceImpl;
import com.axonivy.market.stable.service.ProductMarketplaceDataService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ProductMarketplaceDataServiceImpl
    extends CoreProductMarketplaceDataServiceImpl
    implements ProductMarketplaceDataService {

  public ProductMarketplaceDataServiceImpl(CoreProductMarketplaceDataRepository coreProductMarketplaceDataRepo,
      CoreProductDesignerInstallationRepository coreProductDesignerInstallationRepo,
      CoreProductRepository coreProductRepo) {
    super(coreProductMarketplaceDataRepo, coreProductDesignerInstallationRepo, coreProductRepo);
  }
}
